package ecommerce.configuration.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtGrantedAuthConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        final Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) {
            log.warn("JWT: missing realm_access claim");
            return Collections.emptyList();
        }

        final Object rolesObj = realmAccess.get("roles");
        if (rolesObj == null) {
            log.warn("JWT: missing realm_access.roles claim");
            return Collections.emptyList();
        }

        if (!(rolesObj instanceof List)) {
            log.warn("JWT: realm_access.roles is not an array");
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        final List<Object> rolesListObj = (List<Object>) rolesObj;
        final List<GrantedAuthority> roles = new ArrayList<>(rolesListObj.size());
        for (final var roleObj : rolesListObj) {
            if (roleObj instanceof String role && !role.trim().isEmpty()) {
                roles.add(new SimpleGrantedAuthority(role));
            } else {
                log.warn("JWT: realm_access.roles list contain invalid value");
                return Collections.emptyList();
            }
        }

        return roles;
    }
    
}
