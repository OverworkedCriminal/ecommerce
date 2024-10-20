package ecommerce.service.products;

import org.springframework.stereotype.Service;

import ecommerce.dto.products.InProduct;
import ecommerce.dto.products.InProductPatch;
import ecommerce.dto.products.InProductFilters;
import ecommerce.dto.products.OutProduct;
import ecommerce.dto.products.OutProductDetails;
import ecommerce.dto.shared.InPagination;
import ecommerce.dto.shared.OutPage;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.repository.products.ProductsRepository;
import ecommerce.repository.products.entity.Product;
import ecommerce.service.categories.CategoriesService;
import ecommerce.service.products.mapper.ProductsMapper;
import ecommerce.service.products.mapper.ProductsSpecificationMapper;
import ecommerce.service.utils.mapper.PaginationMapper;
import ecommerce.service.utils.sanitizer.IUserInputSanitizer;
import jakarta.persistence.criteria.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductsService {

    private final CategoriesService categoriesService;
    private final IUserInputSanitizer productsInputSanitizer;
    private final ProductsRepository productsRepository;
    private final ProductsMapper productsMapper;
    private final ProductsSpecificationMapper productsSpecificationMapper;
    private final PaginationMapper paginationMapper;

    /**
     * Find product by id
     * 
     * @param id
     * @return found product's details
     * @throws NotFoundException product with id does not exist or is inactive
     */
    public OutProductDetails getProduct(long id) throws NotFoundException {
        log.trace("id={}", id);

        final var entity = findProductByIdActive(id);
        log.info("found product with id={}", id);

        final var product = productsMapper.fromEntityDetails(entity);

        return product;
    }

    /**
     * Find active products page with specified filters
     * 
     * @param filters
     * @param pagination
     * @return found products
     */
    public OutPage<OutProduct> getProducts(
        InProductFilters filters,
        InPagination pagination
    ) {
        log.trace("{}", filters);
        log.trace("{}", pagination);

        final var pageRequest = paginationMapper.intoPageRequest(pagination);
        final var specification = productsSpecificationMapper
            .mapToSpecification(filters)
            .and((root, query, cb) -> {
                final Path<Boolean> path = root.get("active");
                return cb.equal(path, true);
            });

        final var entityPage = productsRepository.findAll(specification, pageRequest);
        log.info("found products count={}", entityPage.getNumberOfElements());

        final var outPage = paginationMapper.fromPage(entityPage, productsMapper::fromEntity);
        return outPage;
    }

    /**
     * Create product
     * 
     * @param product
     * @return created product's details
     * @throws NotFoundException when category does not exist
     * @throws ValidationException when 'name' or 'description' is invalid
     */
    public OutProductDetails postProduct(InProduct product) throws NotFoundException, ValidationException {
        log.trace("{}", product);

        final var categoryEntity = categoriesService.findCategoryById(product.category());
        log.info("found category with id={}", categoryEntity.getId());

        var entity = productsMapper.intoEntity(product, categoryEntity);
        entity = productsRepository.save(entity);
        log.info("created product with id={}", entity.getId());

        final var savedProduct = productsMapper.fromEntityDetails(entity);

        return savedProduct;
    }

    /**
     * Delete product by setting 'active' to false
     * 
     * @param id
     * @throws NotFoundException when product does not exist or is inactive
     */
    public void deleteProduct(long id) throws NotFoundException {
        log.trace("id={}", id);

        final var product = findProductByIdActive(id);
        log.info("found product with id={}", id);

        product.setActive(false);

        productsRepository.save(product);
        log.info("deleted product with id={}", id);
    }

    /**
     * Update some properties of the product
     * 
     * @param id
     * @param productPatch
     * @throws NotFoundException
     * <ul>
     *   <li>when product does not exist or is inactive</li>
     *   <li>when updated category does not exist</li>
     * </ul>
     * @throws ValidationException when 'name' or 'description' is invalid
     */
    public void patchProduct(
        long id,
        InProductPatch productPatch
    ) throws NotFoundException, ValidationException {
        log.trace("id={}", id);
        log.trace("{}", productPatch);

        final var product = findProductByIdActive(id);
        log.info("found product with id={}", id);

        if (productPatch.name() != null) {
            final var name = productsInputSanitizer.sanitize(productPatch.name());
            product.setName(name);
        }
        if (productPatch.description() != null) {
            final var description = productsInputSanitizer.sanitize(productPatch.description());
            product.setDescription(description);
        }
        if (productPatch.price() != null) {
            product.setPrice(productPatch.price());
        }
        if (productPatch.category() != null) {
            final var categoryEntity = categoriesService.findCategoryById(productPatch.category());
            product.setCategory(categoryEntity);
        }

        productsRepository.save(product);
        log.info("patched product with id={}", id);
    }

    private Product findProductByIdActive(long id) throws NotFoundException {
        final var product = productsRepository
            .findByIdAndActiveTrue(id)
            .orElseThrow(() -> NotFoundException.product(id));
        return product;
    }
}
