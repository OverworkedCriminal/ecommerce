package ecommerce.dto.products;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InProduct(
    @NotBlank String name,
    @NotBlank String description,
    @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal price,
    @NotNull Long category
) {}
