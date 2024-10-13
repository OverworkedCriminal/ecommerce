package ecommerce.controller.v1;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import ecommerce.dto.addresses.InAddress;
import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.InOrderCompletedAtUpdate;
import ecommerce.dto.orders.InOrderProduct;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
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

    //#region getOrder
    
    private void test_getOrder_authorization(
        HttpStatus expectedStatus,
        @Nullable RequestPostProcessor postProcessor
    ) throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/api/v1/orders/1");
        if (postProcessor != null) {
            requestBuilder = requestBuilder.with(postProcessor);
        }

        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void getOrder_statusCode200() throws Exception {
        test_getOrder_authorization(
            HttpStatus.OK, 
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    @Test
    public void getOrder_unauthorized() throws Exception {
        test_getOrder_authorization(
            HttpStatus.UNAUTHORIZED, 
            null
        );
    }

    @Test
    public void getOrder_notFoundException() throws Exception {
        Mockito
            .doThrow(NotFoundException.class)
            .when(ordersService)
            .getOrder(Mockito.any(), Mockito.anyLong());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/api/v1/orders/1")
                    .with(SecurityMockMvcRequestPostProcessors.jwt())
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.NOT_FOUND));
    }

    //#endregion

    //#region postOrder

    private void test_postOrder_authorization(
        HttpStatus expectedStatus,
        @Nullable RequestPostProcessor postProcessor
    ) throws Exception {
        final var order = new InOrder(
            new InAddress(
                "super straight street", 
                "12", 
                "12-345", 
                "Baldur's Gate", 
                1L
            ),
            List.of(
                new InOrderProduct(1L, 10),
                new InOrderProduct(2L, 15)
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
        final var order = new InOrder(
            new InAddress(
                "super straight street", 
                "12", 
                "12-345", 
                "Baldur's Gate", 
                1L
            ),
            null
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_productsEmpty() throws Exception {
        final var order = new InOrder(
            new InAddress(
                "super straight street", 
                "12", 
                "12-345", 
                "Baldur's Gate", 
                1L
            ),
            Collections.emptyList()
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_productsContainQuantityZero() throws Exception {
        final var order = new InOrder(
            new InAddress(
                "super straight street", 
                "12", 
                "12-345", 
                "Baldur's Gate", 
                1L
            ),
            List.of(
                new InOrderProduct(1L, 0)
            )
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_productsContainNullObject() throws Exception {
        final var products = new ArrayList<InOrderProduct>();
        products.add(null);

        final var order = new InOrder(
            new InAddress(
                "super straight street", 
                "12", 
                "12-345", 
                "Baldur's Gate", 
                1L
            ),
            products
        );

        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_addressNull() throws Exception {
        final var order = new InOrder(
            null, 
            List.of(
                new InOrderProduct(1L, 10)
            )
        );

        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_addressStreetNull() throws Exception {
        final var order = new InOrder(
            new InAddress(
                null,
                "12", 
                "12-345", 
                "Baldur's Gate", 
                1L
            ),
            List.of(
                new InOrderProduct(1L, 10)
            )
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_addressStreetBlank() throws Exception {
        final var order = new InOrder(
            new InAddress(
                "",
                "12", 
                "12-345", 
                "Baldur's Gate", 
                1L
            ),
            List.of(
                new InOrderProduct(1L, 10)
            )
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_addressHouseNull() throws Exception {
        final var order = new InOrder(
            new InAddress(
                "street",
                null, 
                "12-345", 
                "Baldur's Gate", 
                1L
            ),
            List.of(
                new InOrderProduct(1L, 10)
            )
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_addressHouseBlank() throws Exception {
        final var order = new InOrder(
            new InAddress(
                "street",
                "", 
                "12-345", 
                "Baldur's Gate", 
                1L
            ),
            List.of(
                new InOrderProduct(1L, 10)
            )
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_addressPostalCodeNull() throws Exception {
        final var order = new InOrder(
            new InAddress(
                "street",
                "12", 
                null,
                "Baldur's Gate", 
                1L
            ),
            List.of(
                new InOrderProduct(1L, 10)
            )
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_addressPostalCodeBlank() throws Exception {
        final var order = new InOrder(
            new InAddress(
                "street",
                "12", 
                "",
                "Baldur's Gate", 
                1L
            ),
            List.of(
                new InOrderProduct(1L, 10)
            )
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_addressCityNull() throws Exception {
        final var order = new InOrder(
            new InAddress(
                "street",
                "12", 
                "12-345",
                null,
                1L
            ),
            List.of(
                new InOrderProduct(1L, 10)
            )
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_addressCityBlank() throws Exception {
        final var order = new InOrder(
            new InAddress(
                "street",
                "12", 
                "12-345",
                "",
                1L
            ),
            List.of(
                new InOrderProduct(1L, 10)
            )
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_addressCountryNull() throws Exception {
        final var order = new InOrder(
            new InAddress(
                "street",
                "12", 
                "12-345",
                "Baldur's Gate",
                null
            ),
            List.of(
                new InOrderProduct(1L, 10)
            )
        );
        test_postOrder_validation(order);
    }

    @Test
    public void postOrder_notFound() throws Exception {
        Mockito
            .doThrow(NotFoundException.class)
            .when(ordersService)
            .postOrder(Mockito.any(), Mockito.any());

        final var order = new InOrder(
            new InAddress(
                "street",
                "12", 
                "12-345",
                "Baldur's Gate",
                1L
            ),
            List.of(
                new InOrderProduct(1L, 10)
            )
        );

        mvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(order))
                    .with(
                        SecurityMockMvcRequestPostProcessors.jwt()
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.NOT_FOUND));
    }

    //#endregion

    //#region putOrderAddress

    private void test_putOrderAddress_authorization(
        HttpStatus expectedStatus,
        RequestPostProcessor postProcessor
    ) throws Exception {
        final var address = new InAddress(
            "street",
            "12", 
            "12-345",
            "Baldur's Gate",
            1L
        );

        var requestBuilder = MockMvcRequestBuilders
            .put("/api/v1/orders/1/address")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(address));
        if (postProcessor != null) {
            requestBuilder = requestBuilder.with(postProcessor);
        }

        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void putOrderAddress_statusCode204() throws Exception {
        test_putOrderAddress_authorization(
            HttpStatus.NO_CONTENT,
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    @Test
    public void putOrderAddress_unauthorized() throws Exception {
        test_putOrderAddress_authorization(
            HttpStatus.UNAUTHORIZED,
            null
        );
    }

    private void test_putOrderAddress_validation(InAddress address) throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders
                    .put("/api/v1/orders/1/address")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(address))
                    .with(
                        SecurityMockMvcRequestPostProcessors.jwt()
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));
    }
    
    @Test
    public void putOrderAddress_addressStreetNull() throws Exception {
        final var address = new InAddress(
            null,
            "12", 
            "12-345", 
            "Baldur's Gate", 
            1L
        );
        test_putOrderAddress_validation(address);
    }

    @Test
    public void putOrderAddress_addressStreetBlank() throws Exception {
        final var address = new InAddress(
            "",
            "12", 
            "12-345", 
            "Baldur's Gate", 
            1L
        );
        test_putOrderAddress_validation(address);
    }

    @Test
    public void putOrderAddress_addressHouseNull() throws Exception {
        final var address = new InAddress(
            "street",
            null, 
            "12-345", 
            "Baldur's Gate", 
            1L
        );
        test_putOrderAddress_validation(address);
    }

    @Test
    public void putOrderAddress_addressHouseBlank() throws Exception {
        final var address = new InAddress(
            "street",
            "", 
            "12-345", 
            "Baldur's Gate", 
            1L
        );
        test_putOrderAddress_validation(address);
    }

    @Test
    public void putOrderAddress_addressPostalCodeNull() throws Exception {
        final var address = new InAddress(
            "street",
            "12", 
            null,
            "Baldur's Gate", 
            1L
        );
        test_putOrderAddress_validation(address);
    }

    @Test
    public void putOrderAddress_addressPostalCodeBlank() throws Exception {
        final var address = new InAddress(
            "street",
            "12", 
            "",
            "Baldur's Gate", 
            1L
        );
        test_putOrderAddress_validation(address);
    }

    @Test
    public void putOrderAddress_addressCityNull() throws Exception {
        final var address = new InAddress(
            "street",
            "12", 
            "12-345",
            null,
            1L
        );
        test_putOrderAddress_validation(address);
    }

    @Test
    public void putOrderAddress_addressCityBlank() throws Exception {
        final var address = new InAddress(
            "street",
            "12", 
            "12-345",
            "",
            1L
        );
        test_putOrderAddress_validation(address);
    }

    @Test
    public void putOrderAddress_addressCountryNull() throws Exception {
        final var address = new InAddress(
            "street",
            "12", 
            "12-345",
            "Baldur's Gate",
            null
        );
        test_putOrderAddress_validation(address);
    }

    @Test
    public void putOrderAddress_notFoundException() throws Exception {
        final var address = new InAddress(
            "street",
            "12", 
            "12-345",
            "Baldur's Gate",
            1L
        );

        Mockito
            .doThrow(NotFoundException.class)
            .when(ordersService)
            .putOrderAddress(Mockito.any(), Mockito.anyLong(), Mockito.any());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .put("/api/v1/orders/1/address")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(address))
                    .with(
                        SecurityMockMvcRequestPostProcessors.jwt()
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.NOT_FOUND));
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
