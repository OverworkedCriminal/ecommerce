package ecommerce.service.categories.mapper;

import org.springframework.stereotype.Component;

import ecommerce.dto.categories.InCategory;
import ecommerce.dto.categories.OutCategory;
import ecommerce.repository.categories.entity.Category;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoriesMapper {

    public Category intoEntity(InCategory category, Category parentCategory) {
        return Category.builder()
            .name(category.name())
            .parentCategory(parentCategory)
            .build();
    }

    public OutCategory fromEntity(Category category) {
        return OutCategory.builder()
            .id(category.getId())
            .name(category.getName())
            .parentCategory(category.getParentCategory().getId())
            .build();
    }
}
