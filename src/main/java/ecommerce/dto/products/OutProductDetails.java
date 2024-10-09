package ecommerce.dto.products;

import java.math.BigDecimal;

import ecommerce.repository.products.entity.Product;
import lombok.Builder;

@Builder
public record OutProductDetails(
    Long id,
    String name,
    String description,
    BigDecimal price,
    long category
) {
    public static OutProductDetails from(Product product) {
        return OutProductDetails.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .category(product.getCategory().getId())
            .build();
    }
}
