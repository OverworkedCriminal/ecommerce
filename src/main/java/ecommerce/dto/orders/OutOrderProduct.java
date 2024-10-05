package ecommerce.dto.orders;

import ecommerce.dto.products.OutProduct;
import ecommerce.repository.orders.entity.OrderProduct;
import lombok.Builder;

@Builder
public record OutOrderProduct(
    OutProduct product,
    int quantity
) {

    public static OutOrderProduct from(OrderProduct orderProduct) {
        return OutOrderProduct.builder()
            .product(OutProduct.from(orderProduct.getProduct()))
            .quantity(orderProduct.getQuantity())
            .build();
    }
}
