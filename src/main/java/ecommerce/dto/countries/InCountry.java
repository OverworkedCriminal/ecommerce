package ecommerce.dto.countries;

import jakarta.validation.constraints.NotBlank;

public record InCountry(
    @NotBlank String name
) {}
