package ecommerce.dto.shared;

import jakarta.validation.constraints.Min;

public record InPagination(
    @Min(1) int pageSize,
    @Min(0) int pageIdx
) {}
