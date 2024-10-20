package ecommerce.dto.products;

import java.math.BigDecimal;

import org.springframework.lang.Nullable;

import ecommerce.dto.validation.nullablenotblank.NullableNotBlank;

import jakarta.validation.constraints.DecimalMin;

public record InProductFilters(
    @Nullable @NullableNotBlank String name,
    @Nullable @DecimalMin(value = "0", inclusive = true) BigDecimal minPrice,
    @Nullable @DecimalMin(value = "0", inclusive = true) BigDecimal maxPrice,
    @Nullable Long category
) {}