package ecommerce.service.products;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import ecommerce.dto.products.InProduct;
import ecommerce.dto.products.InProductsFilters;
import ecommerce.dto.products.OutProduct;
import ecommerce.dto.shared.InPagination;
import ecommerce.dto.shared.OutPage;
import ecommerce.exception.NotFoundException;
import ecommerce.repository.products.ProductsRepository;
import ecommerce.repository.products.entity.Product;
import jakarta.persistence.criteria.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductsServiceImpl implements ProductsService {

    private final ProductsRepository productsRepository;

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
    public OutProduct postProduct(InProduct product) {
        log.trace("{}", product);

        final var entity = Product.builder()
            .active(true)
            .name(product.name())
            .description(product.description())
            .price(product.price())
            .build();

        final var savedEntity = productsRepository.save(entity);
        log.info("created product with id={}", savedEntity.getId());

        final var savedProduct = OutProduct.from(savedEntity);

        return savedProduct;
    }

    @Override
    public void deleteProduct(long id) {
        log.trace("id={}", id);

        final var product = productsRepository
            .findByIdAndActiveTrue(id)
            .orElseThrow(() -> {
                final var message = "product with id=%s does not exist".formatted(id);
                return new NotFoundException(message);
            });

        product.setActive(false);

        productsRepository.save(product);
        log.info("deleted product with id={}", id);
    }
}
