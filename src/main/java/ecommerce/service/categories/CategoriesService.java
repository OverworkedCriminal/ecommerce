package ecommerce.service.categories;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import ecommerce.dto.categories.InCategory;
import ecommerce.dto.categories.InCategoryPatch;
import ecommerce.dto.categories.OutCategory;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.repository.categories.CategoriesRepository;
import ecommerce.repository.categories.entity.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoriesService implements ICategoriesService {

    private final CategoriesRepository categoriesRepository;

    @Override
    public OutCategory postCategory(InCategory categoryIn) {
        log.trace("{}", categoryIn);

        var categoryEntity = Category.builder()
            .name(categoryIn.name())
            .build();

        if (categoryIn.parentCategory() != null) {
            final var parentCategoryEntity = categoriesRepository
                .findById(categoryIn.parentCategory())
                .orElseThrow(() -> NotFoundException.category(categoryIn.parentCategory()));
            categoryEntity.setParentCategory(parentCategoryEntity);
        }

        try {
            categoryEntity = categoriesRepository.save(categoryEntity);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("category with such name already exist" + e.getMessage());
        }

        log.info("created category with id={}", categoryEntity.getId());

        final var categoryOut = OutCategory.from(categoryEntity);

        return categoryOut;
    }

    @Override
    public void patchCategory(long id, InCategoryPatch patch) {
        log.trace("id={}", id);
        log.trace("{}", patch);

        final var categoryEntity = categoriesRepository
            .findById(id)
            .orElseThrow(() -> NotFoundException.category(id));

        if (patch.name() != null) {
            categoryEntity.setName(patch.name());
        }
        if (patch.parentCategory() != null) {
            final var parentCategoryEntity = categoriesRepository
                .findById(patch.parentCategory())
                .orElseThrow(() -> NotFoundException.category(patch.parentCategory()));
            categoryEntity.setParentCategory(parentCategoryEntity);
        }

        try {
            categoriesRepository.save(categoryEntity);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("category with such name already exist: " + e.getMessage());
        }

        log.info("updated category with id={}", id);
    }

    @Override
    public void deleteCategory(long id) {
        log.trace("id={}", id);

        final var categoryEntity = categoriesRepository
            .findById(id)
            .orElseThrow(() -> NotFoundException.category(id));

        try {
            categoriesRepository.delete(categoryEntity);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("category cannot be removed: " + e.getMessage());
        }
        log.info("deleted category with id={}", id);
    }

}
