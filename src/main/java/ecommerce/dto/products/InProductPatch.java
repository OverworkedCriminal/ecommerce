package ecommerce.dto.products;

import java.math.BigDecimal;

import org.springframework.lang.Nullable;

import ecommerce.dto.validation.nullablenotblank.NullableNotBlank;
import jakarta.validation.constraints.DecimalMin;

public record InProductPatch(
    @Nullable @NullableNotBlank String name,
    @Nullable @NullableNotBlank String description,
    @Nullable @DecimalMin(value = "0.00", inclusive = false) BigDecimal price
) {}
