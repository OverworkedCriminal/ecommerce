package ecommerce.dto.products;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import ecommerce.dto.categories.OutCategory;
import ecommerce.repository.products.entity.Product;
import lombok.Builder;

@Builder
public record OutProductDetails(
    Long id,
    String name,
    String description,
    BigDecimal price,
    List<OutCategory> categories
) {
    public static OutProductDetails from(Product product) {
        return OutProductDetails.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .categories(
                product.getCategories()
                    .stream()
                    .map(OutCategory::from)
                    .collect(Collectors.toList())
            )
            .build();
    }
}
