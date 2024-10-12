package ecommerce.service.categories;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import ecommerce.dto.categories.InCategory;
import ecommerce.dto.categories.InCategoryPatch;
import ecommerce.dto.categories.OutCategory;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.repository.categories.CategoriesRepository;
import ecommerce.repository.categories.entity.Category;
import ecommerce.service.categories.mapper.CategoriesMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoriesService {

    private final CategoriesRepository categoriesRepository;

    /**
     * Finds category by ID or throw NotFoundException when not found
     * 
     * @param id
     * @return
     */
    public Category findCategoryById(long id) {
        final var category = categoriesRepository
            .findById(id)
            .orElseThrow(() -> NotFoundException.category(id));
        return category;
    }

    public List<OutCategory> getCategories() {
        final var categoryEntities = categoriesRepository.findAll();
        log.info("found categories count={}", categoryEntities.size());

        final var outCategories = categoryEntities.stream()
            .map(CategoriesMapper::fromEntity)
            .collect(Collectors.toList());

        return outCategories;
    }

    public OutCategory postCategory(InCategory categoryIn) {
        log.trace("{}", categoryIn);

        Category parentCategoryEntity = null;
        if (categoryIn.parentCategory() != null) {
            parentCategoryEntity = findCategoryById(categoryIn.parentCategory());
        }

        var categoryEntity = CategoriesMapper.intoEntity(categoryIn, parentCategoryEntity);

        try {
            categoryEntity = categoriesRepository.save(categoryEntity);
            log.info("created category with id={}", categoryEntity.getId());

        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("category with such name already exist" + e.getMessage());
        }

        final var categoryOut = CategoriesMapper.fromEntity(categoryEntity);

        return categoryOut;
    }
    
    public void patchCategory(long id, InCategoryPatch patch) {
        log.trace("id={}", id);
        log.trace("{}", patch);

        final var categoryEntity = findCategoryById(id);

        final var name = patch.name();
        if (name != null) {
            categoryEntity.setName(name);
        }

        final var parentCategoryId = patch.parentCategory();
        if (parentCategoryId != null) {
            final var parentCategoryEntity = findCategoryById(parentCategoryId);
            validateNoCategoriesCycle(parentCategoryEntity, categoryEntity);
            categoryEntity.setParentCategory(parentCategoryEntity);
        }

        try {
            categoriesRepository.save(categoryEntity);
            log.info("updated category with id={}", id);

        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("category with such name already exist: " + e.getMessage());
        }
    }
    
    public void deleteCategory(long id) {
        log.trace("id={}", id);

        final var categoryEntity = findCategoryById(id);

        try {
            categoriesRepository.delete(categoryEntity);
            log.info("deleted category with id={}", id);

        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("category cannot be removed: " + e.getMessage());
        }
    }

    private void validateNoCategoriesCycle(Category parent, Category self) {
        if (parent.getId().equals(self.getId())) {
            throw new ValidationException("category cannnot be its own parent");
        }

        Category ptr = parent;
        while (ptr.getParentCategory() != null) {
            ptr = ptr.getParentCategory();
            if (ptr.getId().equals(self.getId())) {
                throw new ValidationException("patching category would cause a cycle");
            }
        }
    }
}
