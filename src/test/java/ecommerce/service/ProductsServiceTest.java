package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import ecommerce.dto.products.InProduct;
import ecommerce.dto.products.InProductFilters;
import ecommerce.dto.products.InProductPatch;
import ecommerce.dto.shared.InPagination;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.repository.categories.CategoriesRepository;
import ecommerce.repository.categories.entity.Category;
import ecommerce.repository.products.ProductsRepository;
import ecommerce.repository.products.entity.Product;
import ecommerce.service.categories.CategoriesService;
import ecommerce.service.products.ProductsService;
import ecommerce.service.products.mapper.ProductsMapper;
import ecommerce.service.products.mapper.ProductsSpecificationMapper;
import ecommerce.service.utils.mapper.PaginationMapper;
import ecommerce.service.utils.sanitizer.IUserInputSanitizer;

public class ProductsServiceTest {

    private CategoriesService categoriesService;
    private IUserInputSanitizer userInputSanitizer;
    private ProductsRepository productsRepository;
    private ProductsMapper productsMapper;
    private ProductsSpecificationMapper productsSpecificationMapper;
    private PaginationMapper paginationMapper;

    // Indirect dependencies
    private CategoriesRepository categoriesRepository;

    @BeforeEach
    public void setupDependencies() throws ValidationException {
        // Indirect dependencies
        categoriesRepository = Mockito.mock(CategoriesRepository.class);

        categoriesService = Mockito.mock(CategoriesService.class);
        userInputSanitizer = Mockito.mock(IUserInputSanitizer.class);
        Mockito
            .when(userInputSanitizer.sanitize(Mockito.anyString()))
            .then(AdditionalAnswers.returnsFirstArg());
        productsRepository = Mockito.mock(ProductsRepository.class);
        productsMapper = new ProductsMapper(userInputSanitizer);
        productsSpecificationMapper = new ProductsSpecificationMapper(categoriesRepository);
        paginationMapper = new PaginationMapper();
    }

    private ProductsService createService() {
        return new ProductsService(
            categoriesService,
            userInputSanitizer,
            productsRepository,
            productsMapper,
            productsSpecificationMapper,
            paginationMapper
        );
    }

    //#region getProduct

    @Test
    public void getProduct_notFound() {
        final Long id = 1L;
        
        Mockito
            .doReturn(Optional.empty())
            .when(productsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.getProduct(id);
        });
    }

    @Test
    public void getProduct() throws NotFoundException {
        final Long id = 1L;
        final var category = Category.builder()
            .id(1L)
            .name("category name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList()) // replaced after creating product
            .build();
        final var product = Product.builder()
            .id(id)
            .active(true)
            .name("product name")
            .description("description")
            .price(BigDecimal.valueOf(15.00))
            .category(category)
            .build();
        category.setProducts(List.of(product));

        Mockito
            .doReturn(Optional.of(product))
            .when(productsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        final var out = service.getProduct(id);

        assertEquals(product.getId(), out.id());
        assertEquals(product.getName(), out.name());
        assertEquals(product.getDescription(), out.description());
        assertEquals(product.getPrice(), out.price());
        assertEquals(product.getCategory().getId(), out.category());
    }

    //#endregion

    //#region getProducts

    @Test
    public void getProducts() {
        final var filters = new InProductFilters(null, null, null, null);
        final var pagination = new InPagination(5, 1);
        final var category = Category.builder()
            .id(1L)
            .name("category name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList()) // replaced after creating products
            .build();
        final var products = List.of(
            Product.builder()
                .id(1L)
                .active(true)
                .name("product name 1")
                .description("description 1")
                .price(BigDecimal.valueOf(15.00))
                .category(category)
                .build(),
            Product.builder()
                .id(2L)
                .active(true)
                .name("product name 2")
                .description("description 2")
                .price(BigDecimal.valueOf(35.00))
                .category(category)
                .build()
        );
        category.setProducts(products);
        final var productsPage = new PageImpl<Product>(
            products,
            PageRequest.of(pagination.pageIdx(), pagination.pageSize()),
            products.size()
        );

        Mockito
            .doReturn(productsPage)
            .when(productsRepository)
            .findAll(Mockito.<Specification<Product>>any(), Mockito.<Pageable>any());

        final var service = createService();

        final var outPage = service.getProducts(filters, pagination);

        assertEquals(pagination.pageIdx(), outPage.pageIdx());
        assertEquals(pagination.pageSize(), outPage.pageSize());
        assertEquals(
            pagination.pageSize() * pagination.pageIdx() + products.size(),
            outPage.totalElements()
        );
        assertEquals(products.size(), outPage.content().size());
        assertEquals(
            pagination.pageIdx() + 1,
            outPage.totalPages()
        );
        assertEquals(products.size(), outPage.content().size());
        for (int i = 0; i < products.size(); ++i) {
            final var product = products.get(i);
            final var outProduct = outPage.content().get(i);
            assertEquals(product.getId(), outProduct.id());
            assertEquals(product.getName(), outProduct.name());
            assertEquals(product.getPrice(), outProduct.price());
            assertEquals(product.getCategory().getId(), outProduct.category());
        }
    }

    // TODO: write filters (specification) tests
    
    //#endregion

    //#region postProduct

    @Test
    public void postProduct_categoryNotFound() throws NotFoundException {
        final Long categoryId = 1L;
        final var inProduct = new InProduct(
            "name",
            "description",
            BigDecimal.valueOf(10.00),
            categoryId
        );

        Mockito
            .doThrow(NotFoundException.class)
            .when(categoriesService)
            .findCategoryById(Mockito.eq(categoryId));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.postProduct(inProduct);
        });
    }

    @Test
    public void postProduct_sanitizerValidationException() throws ValidationException, NotFoundException {
        final Long categoryId = 1L;
        final var inProduct = new InProduct(
            "name",
            "description",
            BigDecimal.valueOf(10.00),
            categoryId
        );
        final var category = Category.builder()
            .id(categoryId)
            .name("category name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();

        Mockito
            .doReturn(category)
            .when(categoriesService)
            .findCategoryById(Mockito.eq(categoryId));
        Mockito
            .doThrow(ValidationException.class)
            .when(userInputSanitizer)
            .sanitize(Mockito.anyString());

        final var service = createService();

        assertThrows(ValidationException.class, () -> {
            service.postProduct(inProduct);
        });
    }

    @Test
    public void postProduct() throws NotFoundException, ValidationException {
        final Long categoryId = 1L;
        final Long productId = 1L;
        final var inProduct = new InProduct(
            "name",
            "description",
            BigDecimal.valueOf(10.00),
            categoryId
        );
        final var category = Category.builder()
            .id(categoryId)
            .name("category name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();
        final var product = Product.builder()
            .id(productId)
            .active(true)
            .name(inProduct.name())
            .description(inProduct.description())
            .price(inProduct.price())
            .category(category)
            .build();

        Mockito
            .doReturn(category)
            .when(categoriesService)
            .findCategoryById(Mockito.eq(categoryId));
        Mockito
            .doReturn(product)
            .when(productsRepository)
            .save(Mockito.any());

        final var service = createService();

        final var out = service.postProduct(inProduct);

        assertEquals(product.getId(), out.id());
        assertEquals(product.getName(), out.name());
        assertEquals(product.getDescription(), out.description());
        assertEquals(product.getPrice(), out.price());
        assertEquals(product.getCategory().getId(), out.category());
        Mockito
            .verify(productsRepository)
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(true, saved.getActive());
                    assertEquals(inProduct.name(), saved.getName());
                    assertEquals(inProduct.description(), saved.getDescription());
                    assertEquals(inProduct.price(), saved.getPrice());
                    assertEquals(inProduct.category(), saved.getCategory().getId());
                })
            );
    }
    
    //#endregion

    //#region

    @Test
    public void deleteProduct_notFoundException() {
        final Long id = 1L;

        Mockito
            .doReturn(Optional.empty())
            .when(productsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.deleteProduct(id);
        });
    }

    @Test
    public void deleteProduct() throws NotFoundException {
        final Long id = 1L;
        final var category = Category.builder()
            .id(1L)
            .name("category name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();
        final var product = Product.builder()
            .id(id)
            .active(true)
            .name("name")
            .description("description")
            .price(BigDecimal.valueOf(10.00))
            .category(category)
            .build();
        category.setProducts(List.of(product));

        Mockito
            .doReturn(Optional.of(product))
            .when(productsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        service.deleteProduct(id);

        Mockito
            .verify(productsRepository, Mockito.times(1))
            .save(Mockito.assertArg((saved) -> {
                assertEquals(id, saved.getId());
                assertEquals(false, saved.getActive());
            }));
    }
    
    //#endregion

    //#region patchProduct

    @Test
    public void patchProduct_notFoundException() {
        final Long id = 1L;
        final var inPatch = new InProductPatch(
            "name", 
            "description", 
            BigDecimal.valueOf(10.00), 
            1L
        );

        Mockito
            .doReturn(Optional.empty())
            .when(productsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.patchProduct(id, inPatch);
        });
    }

    private void test_patchProduct_userInputStringInvalid(
        InProductPatch inPatch
    ) throws ValidationException {
        final Long id = 1L;
        final var category = Category.builder()
            .id(1L)
            .name("category name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();
        final var product = Product.builder()
            .id(id)
            .active(true)
            .name("name")
            .description("description")
            .price(BigDecimal.valueOf(10.00))
            .category(category)
            .build();
        category.setProducts(List.of(product));

        Mockito
            .doReturn(Optional.of(product))
            .when(productsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));
        Mockito
            .doThrow(ValidationException.class)
            .when(userInputSanitizer)
            .sanitize(Mockito.anyString());

        final var service = createService();
        
        assertThrows(ValidationException.class, () -> {
            service.patchProduct(id, inPatch);
        });
    }

    @Test
    public void patchProduct_nameInvalid() throws ValidationException {
        final var inPatch = new InProductPatch("name", null, null, null);
        test_patchProduct_userInputStringInvalid(inPatch);
    }

    @Test
    public void patchProduct_descriptionInvalid() throws ValidationException {
        final var inPatch = new InProductPatch(null, "description", null, null);
        test_patchProduct_userInputStringInvalid(inPatch);
    }

    @Test
    public void patchProduct_categoryNotFound() throws NotFoundException {
        final Long id = 1L;
        final Long newCategoryId = 5L;
        final var category = Category.builder()
            .id(1L)
            .name("category name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();
        final var product = Product.builder()
            .id(id)
            .active(true)
            .name("name")
            .description("description")
            .price(BigDecimal.valueOf(10.00))
            .category(category)
            .build();
        category.setProducts(List.of(product));
        final var inPatch = new InProductPatch(null, null, null, newCategoryId);

        Mockito
            .doReturn(Optional.of(product))
            .when(productsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));
        Mockito
            .doThrow(NotFoundException.class)
            .when(categoriesService)
            .findCategoryById(Mockito.eq(newCategoryId));

        final var service = createService();
        
        assertThrows(NotFoundException.class, () -> {
            service.patchProduct(id, inPatch);
        });
    }

    @Test
    public void patchProduct_fieldsAssigned() throws NotFoundException, ValidationException {
        final Long id = 1L;
        final var newName = "new name";
        final var newDescription = "new description";
        final var newPrice = BigDecimal.valueOf(4.99);
        final var newCategory = 2L;
        final var category1 = Category.builder()
            .id(1L)
            .name("category name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();
        final var category2 = Category.builder()
            .id(newCategory)
            .name("category name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();
        final var product = Product.builder()
            .id(id)
            .active(true)
            .name("name")
            .description("description")
            .price(BigDecimal.valueOf(10.00))
            .category(category1)
            .build();
        category1.setProducts(List.of(product));
        final var inPatch = new InProductPatch(
            newName, 
            newDescription, 
            newPrice,
            newCategory
        );

        Mockito
            .doReturn(Optional.of(product))
            .when(productsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));
        Mockito
            .doReturn(category2)
            .when(categoriesService)
            .findCategoryById(Mockito.eq(newCategory));

        final var service = createService();

        service.patchProduct(id, inPatch);

        Mockito
            .verify(productsRepository)
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(id, saved.getId());
                    assertEquals(newName, saved.getName());
                    assertEquals(newDescription, saved.getDescription());
                    assertEquals(newPrice, saved.getPrice());
                    assertEquals(newCategory, saved.getCategory().getId());
                })
            );
    }

    //#endregion
}
