package ecommerce.dto.orders;

import ecommerce.dto.products.OutProduct;
import ecommerce.repository.orders.entity.OrderProduct;
import ecommerce.service.products.mapper.ProductsMapper;
import lombok.Builder;

@Builder
public record OutOrderProduct(
    OutProduct product,
    Integer quantity
) {

    public static OutOrderProduct from(OrderProduct orderProduct) {
        return OutOrderProduct.builder()
            .product(ProductsMapper.fromEntity(orderProduct.getProduct()))
            .quantity(orderProduct.getQuantity())
            .build();
    }
}
