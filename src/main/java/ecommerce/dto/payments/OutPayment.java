package ecommerce.dto.payments;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record OutPayment(
    Long id,
    Long paymentMethod,
    BigDecimal amount
) {}
