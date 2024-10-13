package ecommerce.service.orders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.dto.addresses.InAddress;
import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.InOrderCompletedAtUpdate;
import ecommerce.dto.orders.InOrderFilters;
import ecommerce.dto.orders.InOrderProduct;
import ecommerce.dto.orders.OutOrder;
import ecommerce.dto.shared.InPagination;
import ecommerce.dto.shared.OutPage;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.repository.addresses.AddressesRepository;
import ecommerce.repository.orders.OrderProductsRepository;
import ecommerce.repository.orders.OrdersRepository;
import ecommerce.repository.orders.entity.Order;
import ecommerce.repository.products.ProductsRepository;
import ecommerce.repository.products.entity.Product;
import ecommerce.service.addresses.mapper.AddressesMapper;
import ecommerce.service.countries.CountriesService;
import ecommerce.service.orders.mapper.OrderProductsMapper;
import ecommerce.service.orders.mapper.OrdersMapper;
import ecommerce.service.orders.mapper.OrdersSpecificationMapper;
import ecommerce.service.utils.AuthUtils;
import ecommerce.service.utils.CollectionUtils;
import ecommerce.service.utils.mapper.PaginationMapper;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrdersService {

    private final CountriesService countriesService;
    private final OrdersSpecificationMapper ordersSpecificationMapper;
    private final OrdersRepository ordersRepository;
    private final OrderProductsRepository orderProductsRepository;
    private final ProductsRepository productsRepository;
    private final AddressesRepository addressesRepository;

    public OutOrder getOrder(Authentication user, long id) throws NotFoundException {
        log.trace("id={}", id);

        final var isUserPrivileged = AuthUtils.userHasAnyRole(
            user,
            AuthRoles.ORDER_SEARCH,
            AuthRoles.ORDER_UPDATE
        );
        final Order orderEntity;
        if (isUserPrivileged) {
            // Priviliged user can view other users' orders
            orderEntity = ordersRepository
                .findById(id)
                .orElseThrow(() -> NotFoundException.order(id));
        } else {
            orderEntity = ordersRepository
                .findByIdAndUsername(id, user.getName())
                .orElseThrow(() -> NotFoundException.order(id, user.getName()));
        }
        log.info("found order with id={}", id);

        final var outOrder = OrdersMapper.fromEntity(orderEntity);
        return outOrder;
    }

    public OutPage<OutOrder> getOrders(Authentication user, InOrderFilters filters, InPagination pagination) {
        log.trace("{}", filters);
        log.trace("{}", pagination);

        final var pageRequest = PaginationMapper.intoPageRequest(pagination);
        final var specification = ordersSpecificationMapper
            .mapToSpecification(filters)
            .and((root, query, cb) -> {
                final Path<String> path = root.get("username");
                final Predicate predicate = cb.equal(path, user.getName());
                return predicate;
            });

        final var entityPage = ordersRepository.findAll(specification, pageRequest);
        log.info("found orders count={}", entityPage.getNumberOfElements());

        final var outPage = PaginationMapper.fromPage(entityPage, OrdersMapper::fromEntity);
        return outPage;
    }

    @Transactional
    public OutOrder postOrder(
        Authentication user,
        InOrder orderIn
    ) throws NotFoundException, ValidationException {
        log.trace("{}", orderIn);

        validatePostOrderNoDuplicatedProducts(orderIn);

        final var addressIn = orderIn.address();
        final var countryEntity = countriesService.findByIdActive(addressIn.country());
        final var addressEntity = AddressesMapper.intoEntity(addressIn, countryEntity);

        final Map<Long, InOrderProduct> orderProductInById = orderIn.products()
            .stream()
            .collect(Collectors.toMap(
                orderProductIn -> orderProductIn.productId(),
                orderProductIn -> orderProductIn
            ));

        final var productIds = orderProductInById.keySet();
        final var productEntities = productsRepository.findByActiveTrueAndIdIn(productIds);
        if (productEntities.size() != productIds.size()) {
            throw new NotFoundException("product not found");
        }
        log.info("found all ordered products count={}", productEntities.size());

        final List<Tuple> orderProducts = productEntities.stream()
            .map(product -> new Tuple(
                orderProductInById.get(product.getId()),
                product
            ))
            .collect(Collectors.toList());

        final var summedPrice = orderProducts.stream()
            .map(orderedProduct -> {
                final var quantity = orderedProduct.inOrderProduct().quantity();
                final var price = orderedProduct.product().getPrice();
                return price.multiply(BigDecimal.valueOf(quantity));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var orderEntity = OrdersMapper.intoEntity(
            orderIn,
            user.getName(),
            addressEntity,
            summedPrice
        );
        final var savedOrderEntity = ordersRepository.save(orderEntity);
        log.info("created order with id={}", orderEntity.getId());

        final var orderProductEntities = orderProducts.stream()
            .map(orderedProduct -> OrderProductsMapper.intoEntity(
                orderedProduct.inOrderProduct(),
                orderedProduct.product(),
                savedOrderEntity
            ))
            .collect(Collectors.toList());
        final var savedOrderProductEntities = orderProductsRepository.saveAll(orderProductEntities);
        log.info("created order products count={}", savedOrderProductEntities.size());

        savedOrderEntity.setOrderProducts(savedOrderProductEntities);
        final var orderOut = OrdersMapper.fromEntity(savedOrderEntity);

        return orderOut;
    }

    public void putOrderAddress(
        Authentication user,
        Long id,
        InAddress address
    ) throws NotFoundException, ConflictException {
        log.trace("id={}", id);
        log.trace("{}", address);

        final var isUserPrivileged = AuthUtils.userHasAnyRole(user, AuthRoles.ORDER_UPDATE);
        final Order orderEntity;
        if (isUserPrivileged) {
            // Privileged users can update other users' orders 
            orderEntity = ordersRepository
                .findById(id)
                .orElseThrow(() -> NotFoundException.order(id));
        } else {
            orderEntity = ordersRepository
                .findByIdAndUsername(id, user.getName())
                .orElseThrow(() -> NotFoundException.order(id, user.getName()));
        }
        log.info("found order with id={}", id);

        if (orderEntity.getCompletedAt() != null) {
            throw ConflictException.orderAlreadyCompleted(id);
        }

        final var countryEntity = countriesService.findByIdActive(address.country());
        final var addressEntity = AddressesMapper.intoEntity(address, countryEntity);
        addressEntity.setId(orderEntity.getAddress().getId());

        addressesRepository.save(addressEntity);
        log.info("updated order with id={} address", orderEntity.getId());
    }

    public void putOrderCompletedAt(
        long id,
        InOrderCompletedAtUpdate update
    ) throws NotFoundException, ConflictException, ValidationException {
        log.trace("id={}", id);
        log.trace("{}", update);

        final var orderEntity = ordersRepository
            .findById(id)
            .orElseThrow(() -> NotFoundException.order(id));

        final var completedAt = orderEntity.getCompletedAt();
        if (completedAt != null) {
            throw ConflictException.orderAlreadyCompleted(id);
        } else if (!update.completedAt().isAfter(orderEntity.getOrderedAt())) {
            throw new ValidationException("completedAt must be after orderedAt");
        }

        orderEntity.setCompletedAt(update.completedAt());
        ordersRepository.save(orderEntity);

        log.info("patched order with id={}", id);
    }

    private void validatePostOrderNoDuplicatedProducts(InOrder order) throws ValidationException {
        final var products = order.products();

        if (CollectionUtils.containsDuplicates(products, InOrderProduct::productId)) {
            throw new ValidationException("order products contain duplicates");
        }
    }
}

record Tuple (
    InOrderProduct inOrderProduct,
    Product product
) {}