package ecommerce.repository.payments;

import org.springframework.data.jpa.repository.JpaRepository;

import ecommerce.repository.payments.entity.Payment;

public interface PaymentsRepository extends JpaRepository<Payment, Long> {

}
