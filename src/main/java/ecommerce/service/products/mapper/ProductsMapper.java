package ecommerce.service.products.mapper;

import ecommerce.dto.products.InProduct;
import ecommerce.dto.products.OutProduct;
import ecommerce.dto.products.OutProductDetails;
import ecommerce.repository.categories.entity.Category;
import ecommerce.repository.products.entity.Product;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductsMapper {

    public static Product intoEntity(InProduct product, Category category) {
        return Product.builder()
            .active(true)
            .name(product.name())
            .description(product.description())
            .price(product.price())
            .category(category)
            .build();
    }

    public static OutProduct fromEntity(Product product) {
        return OutProduct.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .category(product.getCategory().getId())
            .build();
    }

    public static OutProductDetails fromEntityDetails(Product product) {
        return OutProductDetails.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .category(product.getCategory().getId())
            .build();
    }
}
