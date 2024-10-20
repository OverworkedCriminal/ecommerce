package ecommerce.configuration.docs;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.NoArgsConstructor;

@Configuration
@SecurityScheme(
    name = OpenApiConfiguration.BEARER,
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
@NoArgsConstructor
public class OpenApiConfiguration {

    public static final String BEARER = "Bearer";

    @Value("${ecommerce.openapi.url}")
    private String url;

    @Bean
    public OpenAPI openApi() {
        final var server = new Server()
            .url(url);

        final var contact = new Contact()
            .email("tom.notifier@gmail.com")
            .name("OverworkedCriminal");

        final var info = new Info()
            .title("Ecommerce")
            .description("SpringBoot ecommerce sample project using JPA with PostgreSQL database")
            .version("0.1.0")
            .contact(contact);

        final var api = new OpenAPI()
            .info(info)
            .servers(List.of(server));

        return api;
    }
}
