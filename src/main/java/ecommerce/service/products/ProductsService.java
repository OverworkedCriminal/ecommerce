package ecommerce.service.products;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import ecommerce.dto.products.InProduct;
import ecommerce.dto.products.InProductPatch;
import ecommerce.dto.products.InProductsFilters;
import ecommerce.dto.products.OutProduct;
import ecommerce.dto.products.OutProductDetails;
import ecommerce.dto.shared.InPagination;
import ecommerce.dto.shared.OutPage;
import ecommerce.exception.NotFoundException;
import ecommerce.repository.products.ProductsRepository;
import ecommerce.repository.products.entity.Product;
import ecommerce.service.categories.CategoriesService;
import ecommerce.service.products.mapper.ProductsMapper;
import ecommerce.service.products.mapper.ProductsSpecificationMapper;
import jakarta.persistence.criteria.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductsService {

    private final CategoriesService categoriesService;
    private final ProductsRepository productsRepository;
    private final ProductsSpecificationMapper productsSpecificationMapper;

    public OutPage<OutProduct> getProducts(
        InProductsFilters filters,
        InPagination pagination
    ) {
        log.trace("{}", filters);
        log.trace("{}", pagination);

        final var pageRequest = PageRequest.of(
            pagination.pageIdx(),
            pagination.pageSize()
        );
        final var specification = productsSpecificationMapper
            .mapToSpecification(filters)
            .and((root, query, cb) -> {
                final Path<Boolean> path = root.get("active");
                return cb.equal(path, true);
            });

        final var products = productsRepository
            .findAll(specification, pageRequest)
            .map(ProductsMapper::fromEntity);
        log.info("found products count={}", products.getNumberOfElements());

        final var productsPage = OutPage.from(products);

        return productsPage;
    }

    public OutProductDetails postProduct(InProduct product) throws NotFoundException {
        log.trace("{}", product);

        final var categoryEntity = categoriesService.findCategoryById(product.category());
        log.info("found category with id={}", categoryEntity.getId());

        var entity = ProductsMapper.intoEntity(product, categoryEntity);
        entity = productsRepository.save(entity);
        log.info("created product with id={}", entity.getId());

        final var savedProduct = ProductsMapper.fromEntityDetails(entity);

        return savedProduct;
    }

    public OutProductDetails getProduct(long id) throws NotFoundException {
        log.trace("id={}", id);

        final var entity = findProductByIdActive(id);
        log.info("found product with id={}", id);

        final var product = ProductsMapper.fromEntityDetails(entity);

        return product;
    }

    public void deleteProduct(long id) throws NotFoundException {
        log.trace("id={}", id);

        final var product = findProductByIdActive(id);
        log.info("found product with id={}", id);

        product.setActive(false);

        productsRepository.save(product);
        log.info("deleted product with id={}", id);
    }

    public void patchProduct(long id, InProductPatch productPatch) throws NotFoundException {
        log.trace("id={}", id);
        log.trace("{}", productPatch);

        final var product = findProductByIdActive(id);
        log.info("found product with id={}", id);

        if (productPatch.name() != null) {
            product.setName(productPatch.name());
        }
        if (productPatch.description() != null) {
            product.setDescription(productPatch.description());
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
