package ecommerce.dto.orders;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InOrderProduct(
    @NotNull Long productId,
    @NotNull @Min(1) Integer quantity
) {}
