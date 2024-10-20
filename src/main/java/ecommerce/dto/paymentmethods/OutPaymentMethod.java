package ecommerce.dto.paymentmethods;

import lombok.Builder;

@Builder
public record OutPaymentMethod(
    Long id,
    String name,
    String description
) {}
