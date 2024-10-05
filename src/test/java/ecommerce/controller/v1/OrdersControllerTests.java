package ecommerce.controller.v1;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.configuration.auth.JwtAuthConfiguration;
import ecommerce.controller.utils.ControllerTestUtils;
import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.InOrderProduct;
import ecommerce.service.orders.OrdersService;

@WebMvcTest(OrdersController.class)
@Import(JwtAuthConfiguration.class)
public class OrdersControllerTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrdersService ordersService;

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

    //#endregion
}
