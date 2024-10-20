package ecommerce.dto.products;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record OutProductDetails(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Long category
) {}
