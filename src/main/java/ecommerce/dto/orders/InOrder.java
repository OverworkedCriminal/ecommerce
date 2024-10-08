package ecommerce.dto.orders;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record InOrder(
    @NotEmpty @Valid List<@NotNull InOrderProduct> products
) {}
