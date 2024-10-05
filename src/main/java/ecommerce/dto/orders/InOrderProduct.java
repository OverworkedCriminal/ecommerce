package ecommerce.dto.orders;

import jakarta.validation.constraints.Min;

public record InOrderProduct(
    long productId,
    @Min(1) int quantity
) {}
