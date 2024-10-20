package ecommerce.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;

import ecommerce.repository.orders.entity.OrderProduct;

public interface OrderProductsRepository extends JpaRepository<OrderProduct, Long> {

}