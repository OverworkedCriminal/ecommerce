package ecommerce.dto.orders;

import java.util.List;

import ecommerce.dto.addresses.InAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record InOrder(
    @NotNull @Valid InAddress address,
    @NotEmpty @Valid List<@NotNull InOrderProduct> products
) {}
