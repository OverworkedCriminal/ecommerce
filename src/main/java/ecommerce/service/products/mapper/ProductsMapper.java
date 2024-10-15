package ecommerce.service.products.mapper;

import org.springframework.stereotype.Component;

import ecommerce.dto.products.InProduct;
import ecommerce.dto.products.OutProduct;
import ecommerce.dto.products.OutProductDetails;
import ecommerce.exception.ValidationException;
import ecommerce.repository.categories.entity.Category;
import ecommerce.repository.products.entity.Product;
import ecommerce.service.products.sanitizer.IProductsInputSanitizer;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductsMapper {

    private final IProductsInputSanitizer productsInputSanitizer;

    public Product intoEntity(InProduct product, Category category) throws ValidationException {
        final String name = productsInputSanitizer.sanitize(product.name());
        final String description = productsInputSanitizer.sanitize(product.description());

        return Product.builder()
            .active(true)
            .name(name)
            .description(description)
            .price(product.price())
            .category(category)
            .build();
    }

    public OutProduct fromEntity(Product product) {
        return OutProduct.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .category(product.getCategory().getId())
            .build();
    }

    public OutProductDetails fromEntityDetails(Product product) {
        return OutProductDetails.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .category(product.getCategory().getId())
            .build();
    }
}
