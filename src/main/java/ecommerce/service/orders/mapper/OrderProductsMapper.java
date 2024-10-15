package ecommerce.service.orders.mapper;

import org.springframework.stereotype.Component;

import ecommerce.dto.orders.InOrderProduct;
import ecommerce.dto.orders.OutOrderProduct;
import ecommerce.repository.orders.entity.Order;
import ecommerce.repository.orders.entity.OrderProduct;
import ecommerce.repository.products.entity.Product;
import ecommerce.service.products.mapper.ProductsMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderProductsMapper {

    private final ProductsMapper productsMapper;

    public OrderProduct intoEntity(
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

    public OutOrderProduct fromEntity(OrderProduct orderProduct) {
        return OutOrderProduct.builder()
            .product(productsMapper.fromEntity(orderProduct.getProduct()))
            .quantity(orderProduct.getQuantity())
            .build();
    }
}
