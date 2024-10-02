package ecommerce.dto.validation.nullablenotblank;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullableNotBlankValidator implements ConstraintValidator<NullableNotBlank, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            // It's not an error if value is null
            return true;
        }

        final String trimmedValue = value.trim();
        final boolean isEmpty = trimmedValue.isEmpty();

        return !isEmpty;
    }

}
