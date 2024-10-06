package ecommerce.service.products;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
import ecommerce.repository.categories.CategoriesRepository;
import ecommerce.repository.categories.entity.Category;
import ecommerce.repository.products.ProductsRepository;
import ecommerce.repository.products.entity.Product;
import ecommerce.service.utils.CollectionUtils;
import jakarta.persistence.criteria.Path;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductsService implements IProductsService {

    private final ProductsRepository productsRepository;
    private final CategoriesRepository categoriesRepository;

    @Override
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
        final var specification = filters
            .intoSpecification()
            .and((root, query, cb) -> {
                final Path<Boolean> path = root.get("active");
                return cb.equal(path, true);
            });

        final var products = productsRepository
            .findAll(specification, pageRequest)
            .map(OutProduct::from);
        log.info("found products count={}", products.getNumberOfElements());

        final var productsPage = OutPage.from(products);

        return productsPage;
    }

    @Override
    public OutProductDetails postProduct(InProduct product) {
        log.trace("{}", product);

        final var categories = product.categories();
        if (CollectionUtils.containsDuplicates(categories)) {
            throw new ValidationException("product categories contain duplicates");
        }
        final var categoryEntities = findCategoriesByIds(categories);

        final var entity = Product.builder()
            .active(true)
            .name(product.name())
            .description(product.description())
            .price(product.price())
            .categories(categoryEntities)
            .build();

        final var savedEntity = productsRepository.save(entity);
        log.info("created product with id={}", savedEntity.getId());

        final var savedProduct = OutProductDetails.from(savedEntity);

        return savedProduct;
    }

    @Override
    public OutProductDetails getProduct(long id) {
        log.trace("id={}", id);

        final var entity = productsRepository
            .findByIdAndActiveTrue(id)
            .orElseThrow(() -> NotFoundException.product(id));
        log.info("found product with id={}", id);

        final var product = OutProductDetails.from(entity);

        return product;
    }

    @Override
    public void deleteProduct(long id) {
        log.trace("id={}", id);

        final var product = productsRepository
            .findByIdAndActiveTrue(id)
            .orElseThrow(() -> NotFoundException.product(id));

        product.setActive(false);

        productsRepository.save(product);
        log.info("deleted product with id={}", id);
    }

    @Override
    public void patchProduct(long id, InProductPatch productPatch) {
        log.trace("id={}", id);
        log.trace("{}", productPatch);

        final var product = productsRepository
            .findByIdAndActiveTrue(id)
            .orElseThrow(() -> NotFoundException.product(id));

        if (productPatch.name() != null) {
            product.setName(productPatch.name());
        }
        if (productPatch.description() != null) {
            product.setDescription(productPatch.description());
        }
        if (productPatch.price() != null) {
            product.setPrice(productPatch.price());
        }
        final List<Long> categories = productPatch.categories();
        if (categories != null) {
            if (CollectionUtils.containsDuplicates(categories)) {
                throw new ValidationException("product categories contain duplicates");
            }
            final var categoryEntities = findCategoriesByIds(categories);
            product.setCategories(categoryEntities);
        }

        productsRepository.save(product);
        log.info("patched product with id={}", id);
    }

    /**
     * Finds categories by ids.
     * 
     * Throws NotFoundException when any of ids is not found.
     * 
     * @param ids
     * @return categories
     */
    private List<Category> findCategoriesByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        final var categories = categoriesRepository.findAllById(ids);
        if (categories.size() != ids.size()) {
            throw new NotFoundException("category not found");
        }

        return categories;
    }
}
