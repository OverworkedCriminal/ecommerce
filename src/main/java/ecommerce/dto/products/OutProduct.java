package ecommerce.dto.products;

import java.math.BigDecimal;

import ecommerce.repository.orders.entity.OrderProduct;
import ecommerce.repository.products.entity.Product;
import lombok.Builder;

@Builder
public record OutProduct(
    Long id,
    String name,
    BigDecimal price,
    long category
) {
    public static OutProduct from(Product product) {
        return OutProduct.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .category(product.getCategory().getId())
            .build();
    }

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
