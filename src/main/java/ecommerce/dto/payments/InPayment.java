package ecommerce.dto.payments;

import jakarta.validation.constraints.NotNull;

public record InPayment(
    @NotNull Long paymentMethod
) {}
