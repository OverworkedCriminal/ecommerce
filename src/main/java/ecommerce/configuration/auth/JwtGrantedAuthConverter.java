package ecommerce.configuration.auth;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import ecommerce.configuration.auth.exception.JwtAuthException;

public class JwtGrantedAuthConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        final Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) {
            return Collections.emptyList();
        }

        final Object rolesObj = realmAccess.get("roles");
        if (rolesObj == null) {
            return Collections.emptyList();
        }

        if (!(rolesObj instanceof List)) {
            throw new RuntimeException("invalid JWT: roles is not an array");
        }

        @SuppressWarnings("unchecked")
        final List<Object> rolesListObj = (List<Object>) rolesObj;
        final Collection<GrantedAuthority> roles = rolesListObj.stream()
                .map(roleObj -> {
                    if (roleObj instanceof String role && !role.trim().isEmpty()) {
                        return new SimpleGrantedAuthority(role);
                    } else {
                        throw new JwtAuthException("invalid JWT: claim realm_access.roles list contain invalid value");
                    }
                })
                .collect(Collectors.toList());


        return roles;
    }
    
}
