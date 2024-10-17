package ecommerce.repository.paymentmethods;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ecommerce.repository.paymentmethods.entity.PaymentMethod;

public interface PaymentMethodsRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByActiveTrue();

    Optional<PaymentMethod> findByIdAndActiveTrue(long id);
}
