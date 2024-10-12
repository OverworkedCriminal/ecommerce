package ecommerce.service.orders.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.OutOrder;
import ecommerce.repository.addresses.entity.Address;
import ecommerce.repository.orders.entity.Order;
import ecommerce.service.addresses.mapper.AddressesMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrdersMapper {

    public static Order intoEntity(
        InOrder order,
        String username,
        Address address,
        BigDecimal price
    ) {
        return Order.builder()
            .username(username)
            .address(address)
            .orderedAt(LocalDateTime.now())
            .completedAt(null)
            .price(price)
            .build();
    }

    public static OutOrder fromEntity(Order order) {
        return OutOrder.builder()
            .id(order.getId())
            .username(order.getUsername())
            .address(AddressesMapper.fromEntity(order.getAddress()))
            .orderedAt(order.getOrderedAt())
            .completedAt(order.getCompletedAt())
            .price(order.getPrice())
            .orderProducts(
                order.getOrderProducts()
                    .stream()
                    .map(OrderProductsMapper::fromEntity)
                    .collect(Collectors.toList())
            )
            .build();
    }
}
