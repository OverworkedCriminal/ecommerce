package ecommerce.dto.products;

import java.math.BigDecimal;

import ecommerce.repository.orders.entity.OrderProduct;
import lombok.Builder;

@Builder
public record OutProduct(
    Long id,
    String name,
    BigDecimal price,
    Long category
) {

    public static OutProduct from(OrderProduct orderProduct) {
        final var product = orderProduct.getProduct();

        return OutProduct.builder()
            .id(product.getId())
            .name(product.getName())
            .price(orderProduct.getPrice())
            .category(product.getCategory().getId())
            .build();
    }
}
