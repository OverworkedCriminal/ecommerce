package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import ecommerce.dto.categories.InCategory;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.repository.categories.CategoriesRepository;
import ecommerce.repository.categories.entity.Category;
import ecommerce.service.categories.CategoriesService;
import ecommerce.service.categories.mapper.CategoriesMapper;

public class CategoriesServiceTest {

    private CategoriesMapper categoriesMapper;
    private CategoriesRepository categoriesRepository;

    @BeforeEach
    public void setupDependencies() {
        // Tests use real implementation of the mapper
        // because mocking it is pointless as it does
        // not contain any business logic
        categoriesMapper = new CategoriesMapper();
        categoriesRepository = Mockito.mock(CategoriesRepository.class);
    }

    /**
     * Creates service for testing purposes.
     * It uses dependencies from class fields.
     * 
     * @return
     */
    private CategoriesService createService() {
        return new CategoriesService(categoriesMapper, categoriesRepository);
    }

    //#region findCategoryById

    @Test
    public void findCategoryById() throws Exception {
        Long id = 124L;

        final var category = Category.builder()
            .id(id)
            .name("name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();

        Mockito
            .doReturn(Optional.of(category))
            .when(categoriesRepository)
            .findById(Mockito.eq(id));

        final var service = createService();

        final var result = service.findCategoryById(id);

        assertEquals(category.getId(), result.getId());
        assertEquals(category.getName(), result.getName());
        assertEquals(category.getParentCategory(), result.getParentCategory());
        assertEquals(category.getChildCategories(), result.getChildCategories());
        assertEquals(category.getProducts(), result.getProducts());
    }

    @Test
    public void findCategoryById_notFoundException() {
        Mockito
            .doReturn(Optional.empty())
            .when(categoriesRepository)
            .findById(Mockito.anyLong());

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.findCategoryById(123L);
        });
    }

    //#endregion

    //#region getCategories

    @Test
    public void getCategories() {
        final var categories = List.of(
            Category.builder()
                .id(1L)
                .name("name 1")
                .parentCategory(null)
                .childCategories(Collections.emptyList())
                .products(Collections.emptyList())
                .build(),
            Category.builder()
                .id(3L)
                .name("name 3")
                .parentCategory(
                    Category.builder()
                        .id(2L)
                        .name("name 2")
                        .parentCategory(null)
                        .childCategories(Collections.emptyList())
                        .products(Collections.emptyList())
                        .build()
                )
                .childCategories(Collections.emptyList())
                .products(Collections.emptyList())
                .build()
        );

        Mockito
            .doReturn(categories)
            .when(categoriesRepository)
            .findAll();

        final var service = createService();

        final var outCategories = service.getCategories();

        assertEquals(categories.size(), outCategories.size());
        for (int i = 0; i < categories.size(); ++i) {
            final var category = categories.get(i);
            final var outCategory = outCategories.get(i);
            assertEquals(category.getId(), outCategory.id());
            assertEquals(category.getName(), outCategory.name());
            assertEquals(
                Optional.ofNullable(category.getParentCategory())
                    .map(Category::getId)
                    .orElse(null),
                outCategory.parentCategory()
            );
        }
    }

    //#endregion

    //#region postCategory

    @Test
    public void postCategory_parentCategoryNotFound() {
        Mockito
            .doReturn(Optional.empty())
            .when(categoriesRepository)
            .findById(Mockito.anyLong());

        final var inCategory = new InCategory("name", 1L);

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.postCategory(inCategory);
        });
    }

    @Test
    public void postCategory_categoryWithNameAlreadyExist() {
        Mockito
            .doThrow(DataIntegrityViolationException.class)
            .when(categoriesRepository)
            .save(Mockito.any());

        final var inCategory = new InCategory("name", null);

        final var service = createService();

        assertThrows(ConflictException.class, () -> {
            service.postCategory(inCategory);
        });
    }

    @Test
    public void postCategory() throws NotFoundException, ConflictException {
        final var inCategory = new InCategory("name", null);
        final var category = Category.builder()
            .id(1L)
            .name(inCategory.name())
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();

        Mockito
            .doReturn(category)
            .when(categoriesRepository)
            .save(Mockito.any());

        final var service = createService();

        final var out = service.postCategory(inCategory);

        assertEquals(category.getId(), out.id());
        assertEquals(category.getName(), out.name());
        assertNull(out.parentCategory());
    }

    @Test
    public void postCategory_withParentCategory() throws NotFoundException, ConflictException {
        final var parentCategory = Category.builder()
            .id(1L)
            .name("parent name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();
        final var category = Category.builder()
            .id(2L)
            .name("name")
            .parentCategory(parentCategory)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();

        Mockito
            .doReturn(Optional.of(parentCategory))
            .when(categoriesRepository)
            .findById(Mockito.eq(parentCategory.getId()));
        Mockito
            .doReturn(category)
            .when(categoriesRepository)
            .save(Mockito.any());

        final var inCategory = new InCategory("new name", parentCategory.getId());

        final var service = createService();

        final var out = service.postCategory(inCategory);

        assertEquals(category.getId(), out.id());
        assertEquals(category.getName(), out.name());
        assertEquals(category.getParentCategory().getId(), out.parentCategory());
    }

    //#endregion
    
    //#region putCategory

    @Test
    public void putCategory_categoryNotFound() {
        final Long id = 1L;
        
        Mockito
            .doReturn(Optional.empty())
            .when(categoriesRepository)
            .findById(Mockito.eq(id));

        final var inCategory = new InCategory("name", null);

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.putCategory(id, inCategory);
        });
    }

    @Test
    public void putCategory_parentCategoryNotFound() {
        final Long id = 2L;
        final Long parentId = 1L;

        final var category = Category.builder()
            .id(id)
            .name("category name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();

        Mockito
            .doReturn(Optional.of(category))
            .when(categoriesRepository)
            .findById(Mockito.eq(id));
        Mockito
            .doReturn(Optional.empty())
            .when(categoriesRepository)
            .findById(Mockito.eq(parentId));

        final var inCategory = new InCategory("name", parentId);

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.putCategory(id, inCategory);
        });
    }

    @Test
    public void putCategory_categoryWithNameAlreadyExist() {
        final Long id = 1L;
        final var category = Category.builder()
            .id(id)
            .name("category name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();

        Mockito
            .doReturn(Optional.of(category))
            .when(categoriesRepository)
            .findById(Mockito.eq(id));
        Mockito
            .doThrow(DataIntegrityViolationException.class)
            .when(categoriesRepository)
            .save(Mockito.any());

        final var inCategory = new InCategory("name", null);

        final var service = createService();

        assertThrows(ConflictException.class, () -> {
            service.putCategory(id, inCategory);
        });
    }

    @Test
    public void putCategory_setParentToSelf() {
        final long id = 1L;
        final var category = Category.builder()
            .id(id)
            .name("category name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();

        Mockito
            .doReturn(Optional.of(category))
            .when(categoriesRepository)
            .findById(Mockito.eq(id));

        final var inCategory = new InCategory("name", id);

        final var service = createService();

        assertThrows(ValidationException.class, () -> {
            service.putCategory(id, inCategory);
        });
    }

    @Test
    public void putCategory_cycleDetected() {
        final var category1 = Category.builder()
            .id(1L)
            .name("category 1")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();
        final var category2 = Category.builder()
            .id(2L)
            .name("category 2")
            .parentCategory(category1)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();
        category1.setChildCategories(List.of(category2));

        Mockito
            .doReturn(Optional.of(category1))
            .when(categoriesRepository)
            .findById(Mockito.eq(category1.getId()));
        Mockito
            .doReturn(Optional.of(category2))
            .when(categoriesRepository)
            .findById(Mockito.eq(category2.getId()));

        final var inCategory = new InCategory("category 1", category2.getId());

        final var service = createService();

        assertThrows(ValidationException.class, () -> {
            service.putCategory(category1.getId(), inCategory);
        });
    }

    @Test
    public void putCategory() throws NotFoundException, ConflictException, ValidationException {
        final var category1 = Category.builder()
            .id(1L)
            .name("name 1")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();
        final var category2 = Category.builder()
            .id(2L)
            .name("name 2")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();

        Mockito
            .doReturn(Optional.of(category1))
            .when(categoriesRepository)
            .findById(category1.getId());
        Mockito
            .doReturn(Optional.of(category2))
            .when(categoriesRepository)
            .findById(category2.getId());

        final var inCategory = new InCategory("new name", category2.getId());

        final var service = createService();

        service.putCategory(category1.getId(), inCategory);

        Mockito
            .verify(categoriesRepository, Mockito.times(1))
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(category1.getId(), saved.getId());
                    assertEquals(inCategory.parentCategory(), saved.getParentCategory().getId());
                })
            );
    }

    //#endregion

    //#region deleteCategory

    @Test
    public void deleteCategory_notFound() {
        final Long id = 1L;

        Mockito
            .doReturn(Optional.empty())
            .when(categoriesRepository)
            .findById(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.deleteCategory(id);
        });
    }

    @Test
    public void deleteCategory_cannotRemove() {
        final var category = Category.builder()
            .id(1L)
            .name("name 1")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();

        Mockito
            .doReturn(Optional.of(category))
            .when(categoriesRepository)
            .findById(Mockito.eq(category.getId()));
        Mockito
            .doThrow(DataIntegrityViolationException.class)
            .when(categoriesRepository)
            .delete(Mockito.any());

        final var service = createService();

        assertThrows(ConflictException.class, () -> {
            service.deleteCategory(category.getId());
        });
    }

    @Test
    public void deleteCategory() throws NotFoundException, ConflictException {
        final var category = Category.builder()
            .id(1L)
            .name("name 1")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();

        Mockito
            .doReturn(Optional.of(category))
            .when(categoriesRepository)
            .findById(Mockito.eq(category.getId()));

        final var service = createService();

        service.deleteCategory(category.getId());

        Mockito
            .verify(categoriesRepository, Mockito.times(1))
            .delete(
                Mockito.assertArg((deleted) -> {
                    assertEquals(category.getId(), deleted.getId());
                })
            );
    }
    
    //#endregion
}
