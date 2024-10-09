package ecommerce.configuration.auth;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled=false,
    securedEnabled=true
)
@Slf4j
public class JwtAuthConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChainBean(
        HttpSecurity http,
        JwtDecoder jwtDecoder,
        JwtGrantedAuthConverter jwtAuthConverter
    ) throws Exception {
        final var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtAuthConverter);

        return http
            .csrf((csrf) -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(httpAuthorize ->
                httpAuthorize
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/products",
                        "/api/v1/products/*",
                        "/api/v1/categories"
                    ).permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> {
                oauth2
                    .jwt(jwt -> 
                        jwt
                            .decoder(jwtDecoder)
                            .jwtAuthenticationConverter(jwtAuthenticationConverter)
                    );
            })
            .exceptionHandling(exceptionHandling -> 
                exceptionHandling
                    .authenticationEntryPoint((request, response, exception) -> {
                        log.warn(exception.getMessage());
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    })
                    .accessDeniedHandler((request, response, exception) -> {
                        log.warn(exception.getMessage());
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    })
            )
            .build();
    }

    @Bean
    public JwtGrantedAuthConverter jwtAuthConverterBean() {
        return new JwtGrantedAuthConverter();
    }

    @Bean
    public JwtDecoder jwtDecoderBean(
        @Value("${ecommerce.auth.jwt.hmac.key}") String decodingKey
    ) {
        final var decodingKeyBytes = decodingKey.getBytes();
        final var key = new SecretKeySpec(decodingKeyBytes, "HmacSHA256");

        return NimbusJwtDecoder
            .withSecretKey(key)
            .build();
    }
}
