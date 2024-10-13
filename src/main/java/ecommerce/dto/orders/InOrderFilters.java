package ecommerce.dto.orders;

import org.springframework.lang.Nullable;

public record InOrderFilters(
    @Nullable Boolean completed
) {}
