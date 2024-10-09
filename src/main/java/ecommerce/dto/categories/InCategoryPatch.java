package ecommerce.dto.categories;

import ecommerce.dto.validation.nullablenotblank.NullableNotBlank;
import jakarta.annotation.Nullable;

public record InCategoryPatch(
    @NullableNotBlank String name,
    @Nullable Long parentCategory
) {}
