package ecommerce.controller.utils;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultMatcher;

public class ControllerTestUtils {

    /**
     * Function creates response status code matcher
     * 
     * @param statusCode
     * @return matcher that checks whether statusCode from response
     * equals passed argument
     */
    public static ResultMatcher expectStatus(HttpStatus status) {
        return result -> {
            final var response = result.getResponse();
            final var responseStatus = response.getStatus();
            Assertions.assertEquals(status.value(), responseStatus);
        };
    }
}
