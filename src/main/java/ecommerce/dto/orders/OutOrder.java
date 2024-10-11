package ecommerce.dto.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import ecommerce.dto.addresses.OutAddress;
import ecommerce.repository.orders.entity.Order;
import lombok.Builder;

@Builder
public record OutOrder(
    Long id,
    String username,
    OutAddress address,
    LocalDateTime orderedAt,
    LocalDateTime completedAt,
    BigDecimal price,
    List<OutOrderProduct> orderProducts
) {

    public static OutOrder from(Order order) {
        return OutOrder.builder()
            .id(order.getId())
            .username(order.getUsername())
            .address(OutAddress.from(order.getAddress()))
            .orderedAt(order.getOrderedAt())
            .completedAt(order.getCompletedAt())
            .price(order.getPrice())
            .orderProducts(
                order.getOrderProducts()
                    .stream()
                    .map(OutOrderProduct::from)
                    .collect(Collectors.toList())
            )
            .build();
    }
}
