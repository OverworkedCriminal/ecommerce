package ecommerce.dto.categories;

import ecommerce.repository.categories.entity.Category;
import lombok.Builder;

@Builder
public record OutCategory(
    Long id,
    String name
) {

    public static OutCategory from(Category category) {
        return OutCategory.builder()
            .id(category.getId())
            .name(category.getName())
            .build();
    }
}
