package ecommerce.dto.payments;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record InPaymentCompletedAtUpdate(
    @NotNull LocalDateTime completedAt
) {}
