package ecommerce.dto.products;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record InProduct(
    @NotBlank String name,
    @NotBlank String description,
    @NonNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal price
) {}
