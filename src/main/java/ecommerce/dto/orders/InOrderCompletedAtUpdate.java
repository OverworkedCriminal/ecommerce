package ecommerce.dto.orders;

import java.time.LocalDateTime;

import jakarta.annotation.Nonnull;

public record InOrderCompletedAtUpdate(
    @Nonnull LocalDateTime completedAt
) {}
