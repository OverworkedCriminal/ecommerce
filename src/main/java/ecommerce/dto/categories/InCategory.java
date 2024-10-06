package ecommerce.dto.categories;

import jakarta.validation.constraints.NotBlank;

public record InCategory(
    @NotBlank String name
) {}
