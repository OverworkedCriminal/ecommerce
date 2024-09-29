package ecommerce.controller.utils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.springframework.test.web.servlet.ResultMatcher;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class ControllerTestUtils {

    /**
     * Function creates response status code matcher
     * 
     * @param statusCode
     * @return matcher that checks whether statusCode from response
     * equals passed argument
     */
    public static ResultMatcher expectStatusCode(int statusCode) {
        return result -> {
            final var response = result.getResponse();
            final var responseStatus = response.getStatus();
            Assertions.assertEquals(statusCode, responseStatus);
        };
    }

    /**
     * Function creates Authorization header value for user with
     * specified roles
     * 
     * @return String "Bearer << JWT >>"
     */
    public static String createAuthorizationBearer(
        String... roles
    ) {
        final var realmAccess = new HashMap<String, List<String>>();
        realmAccess.put("roles", List.of(roles));

        final var jwt = JWT.create()
            .withSubject(UUID.randomUUID().toString())
            .withExpiresAt(Instant.MAX)
            .withClaim("realm_access", realmAccess)
            .sign(Algorithm.HMAC256("secret"));
        final var authorizationBearer = "Bearer " + jwt;

        return authorizationBearer;
    }
}
