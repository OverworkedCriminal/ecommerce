package ecommerce.dto.orders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import ecommerce.repository.orders.entity.Order;
import lombok.Builder;

@Builder
public record OutOrder(
    Long id,
    String username,
    LocalDateTime orderedAt,
    LocalDateTime completedAt,
    List<OutOrderProduct> orderProducts
) {

    public static OutOrder from(Order order) {
        return OutOrder.builder()
            .id(order.getId())
            .username(order.getUsername())
            .orderedAt(order.getOrderedAt())
            .completedAt(order.getCompletedAt())
            .orderProducts(
                order.getOrderProducts()
                    .stream()
                    .map(OutOrderProduct::from)
                    .collect(Collectors.toList())
            )
            .build();
    }
}
