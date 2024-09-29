package ecommerce.dto.products;

import ecommerce.repository.products.entity.Product;
import lombok.Builder;

@Builder
public record OutProduct(
    Long id,
    String name,
    String description
) {
    public static OutProduct from(Product product) {
        return OutProduct.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .build();
    }
}
