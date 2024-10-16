package ecommerce.service.utils.sanitizer;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import ecommerce.exception.ValidationException;

@Component
@Profile(value = "no_sanitizer")
public class PassthroughUserInputSanitizer implements IUserInputSanitizer {

    @Override
    public String sanitize(String userInput) throws ValidationException {
        return userInput;
    }

}
