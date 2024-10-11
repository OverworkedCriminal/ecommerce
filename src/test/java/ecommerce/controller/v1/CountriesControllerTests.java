package ecommerce.controller.v1;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.configuration.auth.JwtAuthConfiguration;
import ecommerce.controller.utils.ControllerTestUtils;
import ecommerce.dto.countries.InCountry;
import ecommerce.exception.ConflictException;
import ecommerce.service.countries.CountriesService;
import jakarta.annotation.Nullable;

@WebMvcTest(CountriesController.class)
@Import(JwtAuthConfiguration.class)
public class CountriesControllerTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CountriesService countriesService;

    private void test_postCountry_authorization(
        HttpStatus expectedStatus,
        @Nullable RequestPostProcessor postProcessor
    ) throws Exception {
        final var country = new InCountry("name");

        var requestBuilder = MockMvcRequestBuilders
            .post("/api/v1/countries")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(country));
        if (postProcessor != null) {
            requestBuilder = requestBuilder.with(postProcessor);
        }

        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void postCountry_statusCode200() throws Exception {
        test_postCountry_authorization(
            HttpStatus.OK,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.COUNTRY_MANAGE))
        );
    }

    @Test
    public void postCountry_unauthorized() throws Exception {
        test_postCountry_authorization(
            HttpStatus.UNAUTHORIZED,
            null
        );
    }

    @Test
    public void postCountry_forbidden() throws Exception {
        test_postCountry_authorization(
            HttpStatus.FORBIDDEN,
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    private void test_postCountry_validation(InCountry country) throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/countries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(country))
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.COUNTRY_MANAGE))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));

        Mockito
            .verify(countriesService, Mockito.never())
            .postProduct(Mockito.any());
    }

    @Test
    public void postCountry_nameNull() throws Exception {
        final var country = new InCountry(null);
        
        test_postCountry_validation(country);
    }

    @Test
    public void postCountry_nameBlank() throws Exception {
        final var country = new InCountry("");

        test_postCountry_validation(country);
    }

    @Test
    public void postCountry_conflictException() throws Exception {
        Mockito
            .doThrow(ConflictException.class)
            .when(countriesService)
            .postProduct(Mockito.any());

        final var country = new InCountry("name");

        mvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/countries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(country))
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.COUNTRY_MANAGE))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.CONFLICT));
    }
}
