package ecommerce.service.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import ecommerce.dto.payments.InPaymentCompletedAtUpdate;
import ecommerce.dto.shared.InPagination;
import ecommerce.dto.shared.OutPage;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.repository.addresses.AddressesRepository;
import ecommerce.repository.orders.OrderProductsRepository;
import ecommerce.repository.orders.OrdersRepository;
import ecommerce.repository.orders.entity.Order;
import ecommerce.repository.payments.PaymentsRepository;
import ecommerce.repository.products.ProductsRepository;
import ecommerce.service.addresses.mapper.AddressesMapper;
import ecommerce.service.countries.CountriesService;
import ecommerce.service.orders.mapper.OrderProductsMapper;
import ecommerce.service.orders.mapper.OrdersMapper;
import ecommerce.service.orders.mapper.OrdersSpecificationMapper;
import ecommerce.service.paymentmethods.PaymentMethodsService;
import ecommerce.service.payments.mapper.PaymentsMapper;
import ecommerce.service.utils.AuthUtils;
import ecommerce.service.utils.CollectionUtils;
import ecommerce.service.utils.mapper.PaginationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrdersService {

    private final CountriesService countriesService;
    private final PaymentMethodsService paymentMethodsService;
    private final OrdersMapper ordersMapper;
    private final OrderProductsMapper orderProductsMapper;
    private final AddressesMapper addressesMapper;
    private final PaymentsMapper paymentsMapper;
    private final PaginationMapper paginationMapper;
    private final OrdersSpecificationMapper ordersSpecificationMapper;
    private final OrdersRepository ordersRepository;
    private final OrderProductsRepository orderProductsRepository;
    private final ProductsRepository productsRepository;
    private final AddressesRepository addressesRepository;
    private final PaymentsRepository paymentsRepository;

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

        final var outOrder = ordersMapper.fromEntity(orderEntity);
        return outOrder;
    }

    public OutPage<OutOrder> getOrders(Authentication user, InOrderFilters filters, InPagination pagination) {
        log.trace("{}", filters);
        log.trace("{}", pagination);

        final var isUserPrivileged = AuthUtils.userHasAnyRole(
            user,
            AuthRoles.ORDER_SEARCH,
            AuthRoles.ORDER_UPDATE
        );
        if (!isUserPrivileged) {
            // underpriviliged user can view only his own orders
            filters.setUsername(user.getName());
        }

        final var pageRequest = paginationMapper.intoPageRequest(pagination);
        final var specification = ordersSpecificationMapper.mapToSpecification(filters);

        final var entityPage = ordersRepository.findAll(specification, pageRequest);
        log.info("found orders count={}", entityPage.getNumberOfElements());

        final var outPage = paginationMapper.fromPage(entityPage, ordersMapper::fromEntity);
        return outPage;
    }

    @Transactional
    public OutOrder postOrder(
        Authentication user,
        InOrder orderIn
    ) throws NotFoundException, ValidationException {
        log.trace("{}", orderIn);

        validatePostOrderNoDuplicatedProducts(orderIn);

        final var countryEntity = countriesService.findByIdActive(orderIn.address().country());
        log.info("found country with id={}", countryEntity.getId());

        final var paymentMethodEntity = paymentMethodsService.findByIdActive(orderIn.payment().paymentMethod());
        log.info("found payment method with id={}", paymentMethodEntity.getId());

        final var productIds = orderIn.products()
            .stream()
            .map(product -> product.productId())
            .collect(Collectors.toList());

        final var productEntities = productsRepository.findByActiveTrueAndIdIn(productIds);
        if (productEntities.size() != productIds.size()) {
            throw new NotFoundException("product not found");
        }
        log.info("found all ordered products count={}", productEntities.size());

        // Sorted by ID to make working with products easier
        final var orderInProducts = orderIn.products().stream()
            .sorted((a, b) -> Long.compare(a.productId(), b.productId()))
            .collect(Collectors.toList());
        final var sortedProductEntities = productEntities.stream()
            .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
            .collect(Collectors.toList());

        final var summedPrice = IntStream.range(0, orderInProducts.size())
            .mapToObj(i -> {
                final var quantity = BigDecimal.valueOf(orderInProducts.get(i).quantity());
                final var price = sortedProductEntities.get(i).getPrice();
                return price.multiply(quantity);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var addressEntity = addressesMapper.intoEntity(orderIn.address(), countryEntity);
        final var paymentEntity = paymentsMapper.intoEntity(paymentMethodEntity, summedPrice);
        final var orderEntity = ordersMapper.intoEntity(
            orderIn,
            user.getName(),
            addressEntity,
            paymentEntity
        );
        final var savedOrderEntity = ordersRepository.save(orderEntity);
        log.info("created order with id={}", orderEntity.getId());

        var orderProductEntities = IntStream.range(0, orderInProducts.size())
            .mapToObj(i -> orderProductsMapper.intoEntity(
                orderInProducts.get(i),
                sortedProductEntities.get(i),
                savedOrderEntity
            ))
            .collect(Collectors.toList());
        orderProductEntities = orderProductsRepository.saveAll(orderProductEntities);
        log.info("created order products count={}", orderProductEntities.size());

        savedOrderEntity.setOrderProducts(orderProductEntities);
        final var orderOut = ordersMapper.fromEntity(savedOrderEntity);

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
        final var addressEntity = addressesMapper.intoEntity(address, countryEntity);
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
        log.info("found order with id={}", id);

        final var completedAt = orderEntity.getCompletedAt();
        if (completedAt != null) {
            throw ConflictException.orderAlreadyCompleted(id);
        }
        if (update.completedAt().isAfter(LocalDateTime.now())) {
            throw new ValidationException("completedAt cannot be from the future");
        }
        if (update.completedAt().isBefore(orderEntity.getOrderedAt())) {
            throw new ValidationException("completedAt must be after orderedAt");
        }

        orderEntity.setCompletedAt(update.completedAt());
        ordersRepository.save(orderEntity);

        log.info("patched order with id={}", id);
    }

    public void putOrderPaymentCompletedAt(
        long id,
        InPaymentCompletedAtUpdate update
    ) throws NotFoundException, ConflictException, ValidationException {
        log.trace("id={}", id);
        log.trace("{}", update);

        final var orderEntity = ordersRepository
            .findById(id)
            .orElseThrow(() -> NotFoundException.order(id));
        log.info("found order with id={}", id);

        final var paymentEntity = orderEntity.getPayment();
        final var completedAt = paymentEntity.getCompletedAt();
        if (completedAt != null) {
            throw new ConflictException(
                "order's with id=%d payment has already been completed"
                    .formatted(id)
            );
        }
        if (update.completedAt().isAfter(LocalDateTime.now())) {
            throw new ValidationException("payment's completed at cannot be from the future");
        }
        if (update.completedAt().isBefore(orderEntity.getOrderedAt())) {
            throw new ValidationException("payment's completedAt must be after orderedAt");
        }

        paymentEntity.setCompletedAt(update.completedAt());
        paymentsRepository.save(paymentEntity);
        log.info("updated order's with id={} payment with id={}", orderEntity.getId(), paymentEntity.getId());
    }

    private void validatePostOrderNoDuplicatedProducts(InOrder order) throws ValidationException {
        final var products = order.products();

        if (CollectionUtils.containsDuplicates(products, InOrderProduct::productId)) {
            throw new ValidationException("order products contain duplicates");
        }
    }
}