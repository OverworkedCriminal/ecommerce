package ecommerce.service.orders.mapper;

import ecommerce.dto.orders.InOrderProduct;
import ecommerce.dto.orders.OutOrderProduct;
import ecommerce.repository.orders.entity.Order;
import ecommerce.repository.orders.entity.OrderProduct;
import ecommerce.repository.products.entity.Product;
import ecommerce.service.products.mapper.ProductsMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderProductsMapper {

    public static OrderProduct intoEntity(
        InOrderProduct orderProduct,
        Product product,
        Order order
    ) {
        return OrderProduct.builder()
            .product(product)
            .order(order)
            .price(product.getPrice())
            .quantity(orderProduct.quantity())
            .build();
    }

    public static OutOrderProduct fromEntity(OrderProduct orderProduct) {
        return OutOrderProduct.builder()
            .product(ProductsMapper.fromEntity(orderProduct.getProduct()))
            .quantity(orderProduct.getQuantity())
            .build();
    }
}
