package ecommerce.dto.products;

import jakarta.validation.constraints.NotBlank;

public record InProduct(
    @NotBlank String name,
    @NotBlank String description
) {}
