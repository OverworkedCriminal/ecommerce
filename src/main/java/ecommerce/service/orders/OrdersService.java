package ecommerce.service.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.OutOrder;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.repository.orders.OrdersRepository;
import ecommerce.repository.orders.entity.Order;
import ecommerce.repository.orders.entity.OrderProduct;
import ecommerce.repository.products.ProductsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrdersService implements IOrdersService {

    private final OrdersRepository ordersRepository;
    private final ProductsRepository productsRepository;

    @Override
    @Transactional
    public OutOrder postOrder(
        Authentication user,
        InOrder orderIn
    ) {
        log.trace("{}", orderIn);

        validatePostOrderNoDuplicatedProducts(orderIn);

        // Map<ProductID, Quantity>
        final var productsInQuantity = orderIn.products()
            .stream()
            .collect(Collectors.toMap(
                product -> product.productId(),
                product -> product.quantity())
            );

        final var productIds = productsInQuantity.keySet();
        final var productEntities = productsRepository.findByActiveTrueAndIdIn(productIds);
        if (productEntities.size() != productIds.size()) {
            throw new NotFoundException("product not found");
        }

        log.info("found all ordered products count={}", productEntities.size());

        final var orderProductEntities = productEntities.stream()
            .map(product -> OrderProduct.builder()
                .product(product)
                .price(product.getPrice())
                .quantity(productsInQuantity.get(product.getId()))
                .build()
            )
            .collect(Collectors.toList());
        final var summedPrice = orderProductEntities.stream()
            .map(orderProduct -> {
                final var price = orderProduct.getPrice();
                final var quantity = BigDecimal.valueOf(orderProduct.getQuantity()); 
                return price.multiply(quantity);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var orderEntity = Order.builder()
            .username(user.getName())
            .orderedAt(LocalDateTime.now())
            .completedAt(null)
            .price(summedPrice)
            .orderProducts(orderProductEntities)
            .build();
        orderProductEntities
            .forEach(orderProductEntity -> orderProductEntity.setOrder(orderEntity));

        final var savedOrderEntity = ordersRepository.save(orderEntity);
        log.info("saved order with id={}", savedOrderEntity.getId());

        final var orderOut = OutOrder.from(savedOrderEntity);

        return orderOut;
    }

    private void validatePostOrderNoDuplicatedProducts(InOrder order) {
        final var products = order.products();
        
        final var productIds = new HashSet<Long>();
        final var allProductsUnique = products
            .stream()
            .map(product -> product.productId())
            .allMatch(productId -> productIds.add(productId));

        if (!allProductsUnique) {
            throw new ValidationException("order products contain duplicates");
        }
    }

}
