package ecommerce.controller.v1;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.configuration.auth.JwtAuthConfiguration;
import ecommerce.controller.utils.ControllerTestUtils;
import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.InOrderCompletedAtUpdate;
import ecommerce.dto.orders.InOrderProduct;
import ecommerce.exception.ConflictException;
import ecommerce.exception.ValidationException;
import ecommerce.service.orders.IOrdersService;

@WebMvcTest(OrdersController.class)
@Import(JwtAuthConfiguration.class)
public class OrdersControllerTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IOrdersService ordersService;

    //#region postOrder

    private void test_postOrder_authorization(
        HttpStatus expectedStatus,
        @Nullable RequestPostProcessor postProcessor
    ) throws Exception {
        final var order = new InOrder(
            List.of(
                new InOrderProduct(1, 10),
                new InOrderProduct(2, 15)
            )
        );

        var requestBuilder = MockMvcRequestBuilders
            .post("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(order));
        if (postProcessor != null) {
            requestBuilder = requestBuilder.with(postProcessor);
        }

        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    private void test_postOrder_validation(InOrder order) throws Exception {
        var requestBuilder = MockMvcRequestBuilders
            .post("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(order))
            .with(SecurityMockMvcRequestPostProcessors.jwt());

        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void postOrder_statusCode200() throws Exception {
        test_postOrder_authorization(
            HttpStatus.OK,
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    @Test
    public void postOrder_unauthorized() throws Exception {
        test_postOrder_authorization(HttpStatus.UNAUTHORIZED, null);
    }

    @Test
    public void postOrder_productsNull() throws Exception {
        final var order = new InOrder(null);
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_productsEmpty() throws Exception {
        final var order = new InOrder(Collections.emptyList());
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_productsContainQuantityZero() throws Exception {
        final var order = new InOrder(
            List.of(
                new InOrderProduct(1, 0)
            )
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_productsContainNullObject() throws Exception {
        final var order = new InOrder(
            List.of((InOrderProduct) null)
        );
        test_postOrder_validation(order);
    }

    //#endregion

    //#region putOrderCompletedAt

    private void test_putOrderCompletedAt_authorization(
        HttpStatus expectedStatus,
        @Nullable RequestPostProcessor postProcessor
    ) throws Exception {
        final var update = new InOrderCompletedAtUpdate(
            LocalDateTime.now().plusDays(5)
        );

        var requestBuilder = MockMvcRequestBuilders
            .put("/api/v1/orders/1/completed-at")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(update));
        if (postProcessor != null) {
            requestBuilder = requestBuilder.with(postProcessor);
        }

        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void putOrderCompletedAt_statusCode204() throws Exception {
        test_putOrderCompletedAt_authorization(
            HttpStatus.NO_CONTENT, 
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.ORDER_UPDATE_COMPLETED_AT))
        );
    }

    @Test
    public void putOrderCompletedAt_unauthorized() throws Exception {
        test_putOrderCompletedAt_authorization(
            HttpStatus.UNAUTHORIZED, 
            null
        );
    }

    @Test
    public void putOrderCompletedAt_forbidden() throws Exception {
        test_putOrderCompletedAt_authorization(
            HttpStatus.FORBIDDEN, 
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    @Test
    public void putOrderCompletedAt_validationException() throws Exception {
        final var update = new InOrderCompletedAtUpdate(LocalDateTime.now());

        Mockito
            .doThrow(ValidationException.class)
            .when(ordersService)
            .putOrderCompletedAt(Mockito.anyLong(), Mockito.any());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .put("/api/v1/orders/1/completed-at")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(update))
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.ORDER_UPDATE_COMPLETED_AT))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void putOrderCompletedAt_conflictException() throws Exception {
        final var update = new InOrderCompletedAtUpdate(LocalDateTime.now());

        Mockito
            .doThrow(ConflictException.class)
            .when(ordersService)
            .putOrderCompletedAt(Mockito.anyLong(), Mockito.any());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .put("/api/v1/orders/1/completed-at")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(update))
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.ORDER_UPDATE_COMPLETED_AT))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.CONFLICT));
    }

    //#endregion
}
