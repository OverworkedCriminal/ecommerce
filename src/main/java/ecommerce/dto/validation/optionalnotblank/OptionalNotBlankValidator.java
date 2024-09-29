package ecommerce.dto.validation.optionalnotblank;

import java.util.Optional;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OptionalNotBlankValidator implements ConstraintValidator<OptionalNotBlank, Optional<String>> {

    @Override
    public boolean isValid(Optional<String> value, ConstraintValidatorContext context) {
        if (!value.isPresent()) {
            // It's not an error if value is empty.
            return true;
        }

        final String trimmedValue = value.get().trim();
        final boolean isEmpty = trimmedValue.isEmpty();

        return !isEmpty;
    }

}
