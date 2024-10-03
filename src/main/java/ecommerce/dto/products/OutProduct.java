package ecommerce.dto.products;

import java.math.BigDecimal;

import ecommerce.repository.products.entity.Product;
import lombok.Builder;

@Builder
public record OutProduct(
    Long id,
    String name,
    BigDecimal price
) {
    public static OutProduct from(Product product) {
        return OutProduct.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .build();
    }
}
