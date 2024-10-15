package ecommerce.service.products.sanitizer;

import ecommerce.exception.ValidationException;

/**
 * Interface used to sanitize products' titles and descriptions.
 */
public interface IProductsInputSanitizer {

    /**
     * Sanitize userInput.
     * 
     * This method can be used to validate user input instead of sanitizing.
     * 
     * @param userInput
     * @return sanitized user input
     * @throws ValidationException
     */
    String sanitize(String userInput) throws ValidationException;

}
