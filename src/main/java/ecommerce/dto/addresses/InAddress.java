package ecommerce.dto.addresses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InAddress(
    @NotBlank String street,
    @NotBlank String house,
    @NotBlank String postalCode,
    @NotBlank String city,
    @NotNull Long country
) {}
