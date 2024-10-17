package ecommerce.dto.paymentmethods;

import ecommerce.dto.validation.nullablenotblank.NullableNotBlank;
import jakarta.annotation.Nullable;

public record InPaymentMethodPatch(
    @Nullable @NullableNotBlank String name,
    @Nullable @NullableNotBlank String description
) {}
