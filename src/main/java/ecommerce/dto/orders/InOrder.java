package ecommerce.dto.orders;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record InOrder(
    @NotEmpty @Valid List<InOrderProduct> products
) {}
