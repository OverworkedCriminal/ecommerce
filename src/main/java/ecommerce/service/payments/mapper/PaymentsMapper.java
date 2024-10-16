package ecommerce.service.payments.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import ecommerce.dto.payments.OutPayment;
import ecommerce.repository.paymentmethods.entity.PaymentMethod;
import ecommerce.repository.payments.entity.Payment;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentsMapper {

    public Payment intoEntity(
        PaymentMethod paymentMethod,
        BigDecimal amount
    ) {
        return Payment.builder()
            .paymentMethod(paymentMethod)
            .amount(amount)
            .completedAt(null)
            .build();
    }

    public OutPayment fromEntity(Payment payment) {
        return OutPayment.builder()
            .id(payment.getId())
            .paymentMethod(payment.getPaymentMethod().getId())
            .amount(payment.getAmount())
            .build();
    }
}
