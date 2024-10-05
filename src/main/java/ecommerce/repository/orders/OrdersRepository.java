package ecommerce.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;

import ecommerce.repository.orders.entity.Order;

public interface OrdersRepository extends JpaRepository<Order, Long> {
    
}
