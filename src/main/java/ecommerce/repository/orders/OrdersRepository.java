package ecommerce.repository.orders;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ecommerce.repository.orders.entity.Order;

public interface OrdersRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByIdAndUsername(Long id, String username);
}
