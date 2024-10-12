package ecommerce.service.categories.mapper;

import ecommerce.dto.categories.InCategory;
import ecommerce.dto.categories.OutCategory;
import ecommerce.repository.categories.entity.Category;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoriesMapper {

    public static Category intoEntity(InCategory category, Category parentCategory) {
        return Category.builder()
            .name(category.name())
            .parentCategory(parentCategory)
            .build();
    }

    public static OutCategory fromEntity(Category category) {
        return OutCategory.builder()
            .id(category.getId())
            .name(category.getName())
            .parentCategory(category.getParentCategory().getId())
            .build();
    }
}
