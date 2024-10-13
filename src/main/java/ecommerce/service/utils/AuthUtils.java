package ecommerce.service.utils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthUtils {

    public static boolean userHasAnyRole(
        Authentication user,
        String... requiredRoles
    ) {
        final var userRoles = extractUserRoles(user);
        final var hasAnyRole = Stream.of(requiredRoles)
            .anyMatch(requiredRole -> userRoles.contains(requiredRole));

        return hasAnyRole;
    }

    public static boolean userHasAllRoles(
        Authentication user,
        String... requiredRoles
    ) {
        final var userRoles = extractUserRoles(user);
        final var requredRolesList = Arrays.asList(requiredRoles);
        final var hasAllRoles = userRoles.containsAll(requredRolesList);

        return hasAllRoles;
    }

    private static Set<String> extractUserRoles(Authentication user) {
        final var userRoles = user.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
        return userRoles;
    }
}
