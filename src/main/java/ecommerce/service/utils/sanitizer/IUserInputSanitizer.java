package ecommerce.service.utils.sanitizer;

import ecommerce.exception.ValidationException;

/**
 * Interface used to sanitize strings passed by users
 */
public interface IUserInputSanitizer {

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
