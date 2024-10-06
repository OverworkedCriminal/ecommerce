package ecommerce.dto.products;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import ecommerce.dto.categories.OutCategory;
import ecommerce.repository.orders.entity.OrderProduct;
import ecommerce.repository.products.entity.Product;
import lombok.Builder;

@Builder
public record OutProduct(
    Long id,
    String name,
    BigDecimal price,
    List<OutCategory> categories
) {
    public static OutProduct from(Product product) {
        return OutProduct.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .categories(
                product.getCategories()
                    .stream()
                    .map(OutCategory::from)
                    .collect(Collectors.toList())
            )
            .build();
    }

    public static OutProduct from(OrderProduct orderProduct) {
        final var product = orderProduct.getProduct();

        return OutProduct.builder()
            .id(product.getId())
            .name(product.getName())
            .price(orderProduct.getPrice())
            .categories(
                product.getCategories()
                    .stream()
                    .map(OutCategory::from)
                    .collect(Collectors.toList())
            )
            .build();
    }
}
