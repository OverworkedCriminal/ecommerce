package ecommerce.dto.shared;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InPagination(
    @NotNull @Min(1) Integer pageSize,
    @NotNull @Min(0) Integer pageIdx
) {}
