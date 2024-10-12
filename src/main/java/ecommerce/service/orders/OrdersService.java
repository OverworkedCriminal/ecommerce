package ecommerce.service.orders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ecommerce.dto.addresses.InAddress;
import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.InOrderCompletedAtUpdate;
import ecommerce.dto.orders.InOrderProduct;
import ecommerce.dto.orders.OutOrder;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.repository.addresses.AddressesRepository;
import ecommerce.repository.orders.OrderProductsRepository;
import ecommerce.repository.orders.OrdersRepository;
import ecommerce.repository.products.ProductsRepository;
import ecommerce.repository.products.entity.Product;
import ecommerce.service.addresses.mapper.AddressesMapper;
import ecommerce.service.countries.CountriesService;
import ecommerce.service.orders.mapper.OrderProductsMapper;
import ecommerce.service.orders.mapper.OrdersMapper;
import ecommerce.service.utils.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrdersService {

    private final CountriesService countriesService;
    private final OrdersRepository ordersRepository;
    private final OrderProductsRepository orderProductsRepository;
    private final ProductsRepository productsRepository;
    private final AddressesRepository addressesRepository;

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
    ) throws NotFoundException {
        log.trace("id={}", id);
        log.trace("{}", address);

        final var orderEntity = ordersRepository
            .findByIdAndUsername(id, user.getName())
            .orElseThrow(() -> {
                return new NotFoundException(
                    "order with id=%d does not exist or does not belong to user=%s"
                        .formatted(id, user.getName())
                );
            });

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