package ecommerce.service.products.sanitizer;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import ecommerce.exception.ValidationException;

@Component
@Profile(value = "no_products_sanitizer")
public class PassthroughProductsInputSanitizer implements IProductsInputSanitizer {

    @Override
    public String sanitize(String userInput) throws ValidationException {
        return userInput;
    }

}
