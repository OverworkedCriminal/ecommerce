package ecommerce.dto.categories;

import lombok.Builder;

@Builder
public record OutCategory(
    Long id,
    String name,
    Long parentCategory
) {}
