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
import ecommerce.dto.paymentmethods.InPaymentMethod;
import ecommerce.dto.paymentmethods.InPaymentMethodPatch;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.service.paymentmethods.PaymentMethodsService;

@WebMvcTest(PaymentMethodsController.class)
@Import(JwtAuthConfiguration.class)
public class PaymentMethodsControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentMethodsService paymentMethodsService;

    //#region getPaymentMethods

    @Test
    public void getPaymentMethods_statusCode200() throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/payment-methods")
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.OK));
    }

    //#endregion

    //#region getPaymentMethod

    private void test_getPaymentMethod_statusCode(
        HttpStatus expectedStatus,
        String url
    ) throws Exception {
        mvc
            .perform(MockMvcRequestBuilders.get(url))
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void getPaymentMethod_statusCode200() throws Exception {
        test_getPaymentMethod_statusCode(HttpStatus.OK, "/api/v1/payment-methods/1");
    }

    @Test
    public void getPaymentMethod_idInvalid() throws Exception {
        test_getPaymentMethod_statusCode(
            HttpStatus.BAD_REQUEST,
            "/api/v1/payment-methods/notInt"
        );
    }

    @Test
    public void getPaymentMethod_notFoundException() throws Exception {
        Mockito
            .doThrow(NotFoundException.class)
            .when(paymentMethodsService)
            .getPaymentMethod(Mockito.anyLong());

        test_getPaymentMethod_statusCode(
            HttpStatus.NOT_FOUND,
            "/api/v1/payment-methods/1"
        );
    }

    //#endregion

    //#region postPaymentMethod

    private void test_postPaymentMethod_statusCode(
        HttpStatus expectedStatus,
        InPaymentMethod paymentMethod,
        RequestPostProcessor postProcessor
    ) throws Exception {
        var requestBuilder = MockMvcRequestBuilders
            .post("/api/v1/payment-methods")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(paymentMethod));
        if (postProcessor != null) {
            requestBuilder = requestBuilder.with(postProcessor);
        }
        
        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void postPaymentMethod_statusCode200() throws Exception {
        final var paymentMethod = new InPaymentMethod("name", "description");
        test_postPaymentMethod_statusCode(
            HttpStatus.OK,
            paymentMethod,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.PAYMENT_METHOD_MANAGE))
        );
    }

    @Test
    public void postPaymentMethod_unauthorized() throws Exception {
        final var paymentMethod = new InPaymentMethod("name", "description");
        test_postPaymentMethod_statusCode(
            HttpStatus.UNAUTHORIZED,
            paymentMethod,
            null
        );
    }

    @Test
    public void postPaymentMethod_forbidden() throws Exception {
        final var paymentMethod = new InPaymentMethod("name", "description");
        test_postPaymentMethod_statusCode(
            HttpStatus.FORBIDDEN,
            paymentMethod,
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    private void test_postPaymentMethod_validation(
        InPaymentMethod paymentMethod
    ) throws Exception {
        test_postPaymentMethod_statusCode(
            HttpStatus.BAD_REQUEST,
            paymentMethod,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.PAYMENT_METHOD_MANAGE))
        );
    }

    @Test
    public void postPaymentMethod_nameNull() throws Exception {
        final var paymentMethod = new InPaymentMethod(null, "description");
        test_postPaymentMethod_validation(paymentMethod);
    }

    @Test
    public void postPaymentMethod_nameBlank() throws Exception {
        final var paymentMethod = new InPaymentMethod("", "description");
        test_postPaymentMethod_validation(paymentMethod);
    }

    @Test
    public void postPaymentMethod_descriptionNull() throws Exception {
        final var paymentMethod = new InPaymentMethod("name", null);
        test_postPaymentMethod_validation(paymentMethod);
    }

    @Test
    public void postPaymentMethod_descriptionBlank() throws Exception {
        final var paymentMethod = new InPaymentMethod("name", "");
        test_postPaymentMethod_validation(paymentMethod);
    }

    @Test
    public void postPaymentMethod_validationException() throws Exception {
        Mockito
            .doThrow(ValidationException.class)
            .when(paymentMethodsService)
            .postPaymentMethod(Mockito.any());

        final var paymentMethod = new InPaymentMethod("name", "description");

        test_postPaymentMethod_statusCode(
            HttpStatus.BAD_REQUEST, 
            paymentMethod,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.PAYMENT_METHOD_MANAGE))
        );
    }

    //#endregion

    //#region patchPaymentMethod

    private void test_patchPaymentMethod_statusCode(
        HttpStatus expectedStatus,
        InPaymentMethodPatch patch,
        RequestPostProcessor postProcessor
    ) throws Exception {
        var requestBuilder = MockMvcRequestBuilders
            .patch("/api/v1/payment-methods/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(patch));
        if (postProcessor != null) {
            requestBuilder = requestBuilder.with(postProcessor);
        }

        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void patchPaymentMethod_statusCode204() throws Exception {
        final var patch = new InPaymentMethodPatch("name", "description");
        test_patchPaymentMethod_statusCode(
            HttpStatus.NO_CONTENT,
            patch,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.PAYMENT_METHOD_MANAGE))
        );
    }

    @Test
    public void patchPaymentMethod_unauthorized() throws Exception {
        final var patch = new InPaymentMethodPatch("name", "description");
        test_patchPaymentMethod_statusCode(
            HttpStatus.UNAUTHORIZED,
            patch,
            null
        );
    }

    @Test
    public void patchPaymentMethod_forbidden() throws Exception {
        final var patch = new InPaymentMethodPatch("name", "description");
        test_patchPaymentMethod_statusCode(
            HttpStatus.FORBIDDEN,
            patch,
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    @Test
    public void patchPaymentMethod_idInvalid() throws Exception {
        final var patch = new InPaymentMethodPatch("name", "description");
        mvc
            .perform(
                MockMvcRequestBuilders
                    .patch("/api/v1/payment-methods/notInt")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(patch))
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.PAYMENT_METHOD_MANAGE))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void patchPaymentMethod_notFoundException() throws Exception {
        Mockito
            .doThrow(NotFoundException.class)
            .when(paymentMethodsService)
            .patchPaymentMethod(Mockito.anyLong(), Mockito.any());

        final var patch = new InPaymentMethodPatch("name", "description");

        test_patchPaymentMethod_statusCode(
            HttpStatus.NOT_FOUND, 
            patch,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.PAYMENT_METHOD_MANAGE))
        );
    }

    @Test
    public void patchPaymentMethod_validationException() throws Exception {
        Mockito
            .doThrow(ValidationException.class)
            .when(paymentMethodsService)
            .patchPaymentMethod(Mockito.anyLong(), Mockito.any());

        final var patch = new InPaymentMethodPatch("name", "description");

        test_patchPaymentMethod_statusCode(
            HttpStatus.BAD_REQUEST, 
            patch,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.PAYMENT_METHOD_MANAGE))
        );
    }

    private void test_patchPaymentMethod_validation(
        InPaymentMethodPatch patch
    ) throws Exception {
        test_patchPaymentMethod_statusCode(
            HttpStatus.BAD_REQUEST,
            patch,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.PAYMENT_METHOD_MANAGE))
        );
    }

    @Test
    public void patchPaymentMethod_nameBlank() throws Exception {
        final var patch = new InPaymentMethodPatch("", "description");
        test_patchPaymentMethod_validation(patch);
    }

    @Test
    public void patchPaymentMethod_descriptionBlank() throws Exception {
        final var patch = new InPaymentMethodPatch("name", "");
        test_patchPaymentMethod_validation(patch);
    }

    //#endregion

    //#region deletePaymentMethod

    private void test_deletePaymentMethod_statusCode(
        HttpStatus expectedStatus,
        RequestPostProcessor postProcessor
    ) throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/api/v1/payment-methods/1");
        if (postProcessor != null) {
            requestBuilder = requestBuilder.with(postProcessor);
        }

        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void deletePaymentMethod_statusCode204() throws Exception {
        test_deletePaymentMethod_statusCode(
            HttpStatus.NO_CONTENT,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.PAYMENT_METHOD_MANAGE))
        );
    }

    @Test
    public void deletePaymentMethod_unauthorized() throws Exception {
        test_deletePaymentMethod_statusCode(
            HttpStatus.UNAUTHORIZED,
            null
        );
    }

    @Test
    public void deletePaymentMethod_forbidden() throws Exception {
        test_deletePaymentMethod_statusCode(
            HttpStatus.FORBIDDEN,
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    @Test
    public void deletePaymentMethod_notFoundException() throws Exception {
        Mockito
            .doThrow(NotFoundException.class)
            .when(paymentMethodsService)
            .deletePaymentMethod(Mockito.anyLong());

        test_deletePaymentMethod_statusCode(
            HttpStatus.NOT_FOUND,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.PAYMENT_METHOD_MANAGE))
        );
    }

    @Test
    public void deletePaymentMethod_idInvalid() throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders
                    .delete("/api/v1/payment-methods/notInt")
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.PAYMENT_METHOD_MANAGE))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));
    }

    //#endregion
}
