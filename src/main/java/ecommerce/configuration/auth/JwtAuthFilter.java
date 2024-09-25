package ecommerce.configuration.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import ecommerce.configuration.auth.exception.JwtAuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final Algorithm algorithm;
    private final JWTVerifier jwtVerifier;

    public JwtAuthFilter(
            @Value("${ecommerce.auth.jwt.hmac.key}") String decodingKey
    ) {
        this.algorithm = Algorithm.HMAC256(decodingKey);
        this.jwtVerifier = JWT.require(this.algorithm)
                .ignoreIssuedAt()
                .build();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {
        final String authorizationHeader = request.getHeader("Authorization");

        UsernamePasswordAuthenticationToken auth;
        try {
            auth = this.tryParseAuthorizationHeader(authorizationHeader);
        } catch (JwtAuthException e) {
            // TODO: log some warning
            auth = null;
        }

        SecurityContextHolder
                .getContext()
                .setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    protected UsernamePasswordAuthenticationToken tryParseAuthorizationHeader(
            String authorizationHeader
    ) throws IOException, ServletException {
        if (authorizationHeader == null) {
            // Empty header is not an error.
            // User is not authenticated. It's not needed to throw exception here
            return null;
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new JwtAuthException("unsupported auth-scheme");
        }

        final String jwt = authorizationHeader.substring("Bearer ".length());
        final DecodedJWT decodedJwt;
        try {
            decodedJwt = jwtVerifier.verify(jwt);
        } catch (JWTVerificationException e) {
            throw new JwtAuthException("invalid JWT: " + e.getMessage());
        }

        final String sub = decodedJwt.getSubject();
        if (sub == null) {
            throw new JwtAuthException("invalid JWT: missing sub claim");
        }

        final List<SimpleGrantedAuthority> roles = parseRealmAccessRoles(decodedJwt);
        final var auth = new UsernamePasswordAuthenticationToken(sub, null, roles);

        return auth;
    }

    private static List<SimpleGrantedAuthority> parseRealmAccessRoles(DecodedJWT decodedJwt) throws JwtAuthException {
        final Map<String, Object> realmAccess = decodedJwt.getClaim("realm_access").asMap();
        if (realmAccess == null) {
            // Missing realm_access is not a problem
            // it means user has no roles
            return Collections.emptyList();
        }

        final Object rolesObj = realmAccess.get("roles");
        if (rolesObj == null) {
            // Missing realm access.roles is not a problem
            // it means user has no roles
            return Collections.emptyList();
        }

        if (!(rolesObj instanceof List)) {
            throw new JwtAuthException("invalid JWT: claim realm_access.roles has invalid format");
        }

        @SuppressWarnings("unchecked")
        final List<Object> rolesListObj = (List<Object>) rolesObj;
        final List<SimpleGrantedAuthority> roles = rolesListObj.stream()
                .map(roleObj -> {
                    if (!(roleObj instanceof String)) {
                        throw new JwtAuthException("invalid JWT: claim realm_access.roles list contain invalid value");
                    }
                    return new SimpleGrantedAuthority((String) roleObj);
                })
                .collect(Collectors.toList());

        return roles;
    }

}
