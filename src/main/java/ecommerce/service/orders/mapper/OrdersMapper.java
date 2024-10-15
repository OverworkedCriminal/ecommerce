package ecommerce.service.orders.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.OutOrder;
import ecommerce.repository.addresses.entity.Address;
import ecommerce.repository.orders.entity.Order;
import ecommerce.service.addresses.mapper.AddressesMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrdersMapper {

    private final OrderProductsMapper orderProductsMapper;
    private final AddressesMapper addressesMapper;

    public Order intoEntity(
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

    public OutOrder fromEntity(Order order) {
        return OutOrder.builder()
            .id(order.getId())
            .username(order.getUsername())
            .address(addressesMapper.fromEntity(order.getAddress()))
            .orderedAt(order.getOrderedAt())
            .completedAt(order.getCompletedAt())
            .price(order.getPrice())
            .orderProducts(
                order.getOrderProducts()
                    .stream()
                    .map(orderProductsMapper::fromEntity)
                    .collect(Collectors.toList())
            )
            .build();
    }
}
