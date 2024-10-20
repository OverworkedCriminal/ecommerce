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
import ecommerce.dto.countries.OutCountry;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
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

    //#region getCountry

    private void test_getCountry_responseStatus(HttpStatus expectedStatus) throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/countries/1")
            )
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void getCountry_statusCode200() throws Exception {
        Mockito
            .doReturn(new OutCountry(1L, "name"))
            .when(countriesService)
            .getCountry(Mockito.anyLong());

        test_getCountry_responseStatus(HttpStatus.OK);
    }

    @Test
    public void getCountry_notFound() throws Exception {
        Mockito
            .doThrow(NotFoundException.class)
            .when(countriesService)
            .getCountry(Mockito.anyLong());

        test_getCountry_responseStatus(HttpStatus.NOT_FOUND);
    }

    //#endregion

    //#region getCountries

    @Test
    public void getCountries_statusCode200() throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/countries")
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.OK));
    }

    //#endregion

    //#region postCountry

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
            .postCountry(Mockito.any());
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
            .postCountry(Mockito.any());

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

    //#endregion

    //#region deleteCountry

    private void test_deleteCountry_authorization(
        HttpStatus expectedStatus,
        @Nullable RequestPostProcessor postProcessor
    ) throws Exception {
        var requestBuilder = MockMvcRequestBuilders
            .delete("/api/v1/countries/1");
        if (postProcessor != null) {
            requestBuilder = requestBuilder.with(postProcessor);
        }
        
        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void deleteCountry_statusCode204() throws Exception {
        test_deleteCountry_authorization(
            HttpStatus.NO_CONTENT,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.COUNTRY_MANAGE))
        );
    }

    @Test
    public void deleteCountry_unauthorized() throws Exception {
        test_deleteCountry_authorization(
            HttpStatus.UNAUTHORIZED, 
            null
        );
    }

    @Test
    public void deleteCountry_forbidden() throws Exception {
        test_deleteCountry_authorization(
            HttpStatus.FORBIDDEN, 
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    @Test
    public void deleteCountry_notFound() throws Exception {
        Mockito
            .doThrow(NotFoundException.class)
            .when(countriesService)
            .deleteCountry(Mockito.anyLong());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .delete("/api/v1/countries/1")
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.COUNTRY_MANAGE))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.NOT_FOUND));
    }
    //#endregion
}
