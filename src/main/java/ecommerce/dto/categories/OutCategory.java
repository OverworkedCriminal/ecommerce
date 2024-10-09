package ecommerce.dto.categories;

import java.util.Optional;

import ecommerce.repository.categories.entity.Category;
import lombok.Builder;

@Builder
public record OutCategory(
    Long id,
    String name,
    Long parentCategory
) {

    public static OutCategory from(Category category) {
        return OutCategory.builder()
            .id(category.getId())
            .name(category.getName())
            .parentCategory(
                Optional
                    .ofNullable(category.getParentCategory())
                    .map(Category::getId)
                    .orElse(null)
            )
            .build();
    }
}
