package ecommerce.dto.paymentmethods;

import jakarta.validation.constraints.NotBlank;

public record InPaymentMethod(
    @NotBlank String name,
    @NotBlank String description
) {}
