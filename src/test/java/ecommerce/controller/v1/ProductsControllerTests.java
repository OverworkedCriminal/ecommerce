package ecommerce.controller.v1;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.configuration.auth.JwtAuthConfiguration;
import ecommerce.controller.utils.ControllerTestUtils;
import ecommerce.dto.products.InProduct;
import ecommerce.service.products.ProductsService;

@Import(JwtAuthConfiguration.class)
@WebMvcTest(
    controllers = ProductsController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class    
)
public class ProductsControllerTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductsService productsService;

    //#region getProducts

    @Test
    public void getProducts_pageSizeLessThan1() throws Exception {
        final int pageSize = 0;
        final int pageIdx = 2;
        final String url = "/api/v1/products?pageSize=%d&pageIdx=%d"
            .formatted(pageSize, pageIdx);

        mvc
            .perform(MockMvcRequestBuilders.get(url))
            .andExpect(ControllerTestUtils.expectStatusCode(400));

        Mockito
            .verify(productsService, Mockito.never())
            .getProducts(Mockito.any(), Mockito.any());
    }

    @Test
    public void getProducts_pageIdxLessThan0() throws Exception {
        final int pageSize = 10;
        final int pageIdx = -1;
        final String url = "/api/v1/products?pageSize=%d&pageIdx=%d"
            .formatted(pageSize, pageIdx);

        mvc
            .perform(MockMvcRequestBuilders.get(url))
            .andExpect(ControllerTestUtils.expectStatusCode(400));

        Mockito
            .verify(productsService, Mockito.never())
            .getProducts(Mockito.any(), Mockito.any());
    }

    @Test
    public void getProducts_nameBlank() throws Exception {
        final int pageSize = 10;
        final int pageIdx = 1;
        final String url = "/api/v1/products?pageSize=%d&pageIdx=%d&name="
            .formatted(pageSize, pageIdx);

        mvc
            .perform(MockMvcRequestBuilders.get(url))
            .andExpect(ControllerTestUtils.expectStatusCode(400));

        Mockito
            .verify(productsService, Mockito.never())
            .getProducts(Mockito.any(), Mockito.any());
    }

    //#endregion

    //#region postProduct

    @Test
    public void postProduct_unauthorized() throws Exception {
        final var product = new InProduct(
            "name",
            "description"
        );

        mvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(product))
            )
            .andExpect(ControllerTestUtils.expectStatusCode(401));

        Mockito
            .verify(productsService, Mockito.never())
            .postProduct(Mockito.any());
    }

    @Test
    public void postProduct_forbidden() throws Exception {
        final var product = new InProduct(
            "name",
            "description"
        );

        mvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/products")
                    .header(
                        "Authorization",
                        ControllerTestUtils.createAuthorizationBearer()
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(product))
            )
            .andExpect(ControllerTestUtils.expectStatusCode(403));

        Mockito
            .verify(productsService, Mockito.never())
            .postProduct(Mockito.any());
    }

    @Test
    public void postProduct_blankName() throws Exception {
        final var product = new InProduct(
            "",
            "description"
        );

        mvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/products")
                    .header(
                        "Authorization",
                        ControllerTestUtils.createAuthorizationBearer(
                            AuthRoles.CREATE_PRODUCT
                        )
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(product))
            )
            .andExpect(ControllerTestUtils.expectStatusCode(400));

        Mockito
            .verify(productsService, Mockito.never())
            .postProduct(Mockito.any());
    }

    @Test
    public void postProduct_blankDescription() throws Exception {
        final var product = new InProduct(
            "name",
            ""
        );

        mvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/products")
                    .header(
                        "Authorization",
                        ControllerTestUtils.createAuthorizationBearer(
                            AuthRoles.CREATE_PRODUCT
                        )
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(product))
            )
            .andExpect(ControllerTestUtils.expectStatusCode(400));

        Mockito
            .verify(productsService, Mockito.never())
            .postProduct(Mockito.any());
    }

    //#endregion
}
