package ecommerce.controller.v1;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.configuration.auth.JwtAuthConfiguration;
import ecommerce.controller.utils.ControllerTestUtils;
import ecommerce.dto.products.InProduct;
import ecommerce.dto.products.InProductPatch;
import ecommerce.dto.products.OutProduct;
import ecommerce.exception.NotFoundException;
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

    /**
     * Parametrized test asserts status code 400 is returned for given url
     * and service function is never called
     * 
     * @param url
     * @throws Exception
     */
    private void test_getProducts_validationException(String url) throws Exception {
        mvc
            .perform(MockMvcRequestBuilders.get(url))
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));

        Mockito
            .verify(productsService, Mockito.never())
            .getProducts(Mockito.any(), Mockito.any());
    }

    @Test
    public void getProducts_noFilters() throws Exception {
        final int pageSize = 10;
        final int pageIdx = 2;
        final String url = "/api/v1/products?pageSize=%d&pageIdx=%d"
            .formatted(pageSize, pageIdx);

        mvc
            .perform(MockMvcRequestBuilders.get(url))
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.OK));

        Mockito
            .verify(productsService, Mockito.times(1))
            .getProducts(Mockito.any(), Mockito.any());
    }

    @Test
    public void getProducts_pageSizeLessThan1() throws Exception {
        final int pageSize = 0;
        final int pageIdx = 2;
        final String url = "/api/v1/products?pageSize=%d&pageIdx=%d"
            .formatted(pageSize, pageIdx);

        test_getProducts_validationException(url);
    }

    @Test
    public void getProducts_pageIdxLessThan0() throws Exception {
        final int pageSize = 10;
        final int pageIdx = -1;
        final String url = "/api/v1/products?pageSize=%d&pageIdx=%d"
            .formatted(pageSize, pageIdx);

        test_getProducts_validationException(url);
    }

    @Test
    public void getProducts_nameBlank() throws Exception {
        final int pageSize = 10;
        final int pageIdx = 1;
        final String url = "/api/v1/products?pageSize=%d&pageIdx=%d&name="
            .formatted(pageSize, pageIdx);

        test_getProducts_validationException(url);
    }

    @Test
    public void getProducts_minPriceBelowZero() throws Exception {
        final int pageSize = 10;
        final int pageIdx = 1;
        final BigDecimal minPrice = new BigDecimal(-0.01);
        final String url = "/api/v1/products?pageSize=%d&pageIdx=%d&minPrice=%.2f"
            .formatted(pageSize, pageIdx, minPrice);

        test_getProducts_validationException(url);
    }

    @Test
    public void getProducts_maxPriceBelowZero() throws Exception {
        final int pageSize = 10;
        final int pageIdx = 1;
        final BigDecimal maxPrice = new BigDecimal(-0.01);
        final String url = "/api/v1/products?pageSize=%d&pageIdx=%d&maxPrice=%.2f"
            .formatted(pageSize, pageIdx, maxPrice);

        test_getProducts_validationException(url);
    }

    //#endregion

    //#region postProduct

    /**
     * Parametrized test asserts status code 400 is returned for given product
     * and service function is never called
     * 
     * @param product
     * @throws Exception
     */
    private void test_postProduct_validationException(InProduct product) throws Exception {
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
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));

        Mockito
            .verify(productsService, Mockito.never())
            .postProduct(Mockito.any());
    }

    @Test
    public void postProduct_success() throws Exception {
        final var product = new InProduct(
            "name",
            "description",
            new BigDecimal(10.00)
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
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.OK));

        Mockito
            .verify(productsService, Mockito.times(1))
            .postProduct(Mockito.any());
    }

    @Test
    public void postProduct_unauthorized() throws Exception {
        final var product = new InProduct(
            "name",
            "description",
            new BigDecimal(10.00)
        );

        mvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(product))
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.UNAUTHORIZED));

        Mockito
            .verify(productsService, Mockito.never())
            .postProduct(Mockito.any());
    }

    @Test
    public void postProduct_forbidden() throws Exception {
        final var product = new InProduct(
            "name",
            "description",
            new BigDecimal(10.00)
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
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.FORBIDDEN));

        Mockito
            .verify(productsService, Mockito.never())
            .postProduct(Mockito.any());
    }

    @Test
    public void postProduct_blankName() throws Exception {
        final var product = new InProduct(
            "",
            "description",
            new BigDecimal(10.00)
        );

        test_postProduct_validationException(product);
    }

    @Test
    public void postProduct_blankDescription() throws Exception {
        final var product = new InProduct(
            "name",
            "",
            new BigDecimal(10.00)
        );

        test_postProduct_validationException(product);
    }

    @Test
    public void postProudct_priceZero() throws Exception {
        final var product = new InProduct(
            "name",
            "description",
            BigDecimal.ZERO
        );

        test_postProduct_validationException(product);
    }

    //#endregion

    //#region getProduct

    @Test
    public void getProduct_statusCode200() throws Exception {
        final var product = new OutProduct(
            1L,
            "name",
            "description",
            new BigDecimal(24.99)
        );

        Mockito
            .doReturn(product)
            .when(productsService)
            .getProduct(Mockito.anyLong());

        mvc
            .perform(MockMvcRequestBuilders.get("/api/v1/products/1"))
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.OK));
    }

    @Test
    public void getProduct_notFound() throws Exception {
        Mockito
            .doThrow(NotFoundException.class)
            .when(productsService)
            .getProduct(Mockito.anyLong());

        mvc
            .perform(MockMvcRequestBuilders.get("/api/v1/products/1"))
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.NOT_FOUND));
    }

    //#endregion

    //#region deleteProduct

    @Test
    public void deleteProduct_statusCode204() throws Exception {
        Mockito
            .doNothing()
            .when(productsService)
            .deleteProduct(Mockito.anyLong());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .delete("/api/v1/products/1")
                    .header(
                        "Authorization",
                        ControllerTestUtils.createAuthorizationBearer(AuthRoles.DELETE_PRODUCT)
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.NO_CONTENT));
    }

    @Test
    public void deleteProduct_unauthorized() throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/products/1")
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.UNAUTHORIZED));

        Mockito
            .verify(productsService, Mockito.never())
            .deleteProduct(Mockito.anyLong());
    }

    @Test
    public void deleteProduct_forbidden() throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders
                    .delete("/api/v1/products/1")
                    .header(
                        "Authorization",
                        ControllerTestUtils.createAuthorizationBearer()
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.FORBIDDEN));

        Mockito
            .verify(productsService, Mockito.never())
            .deleteProduct(Mockito.anyLong());
    }

    @Test
    public void deleteProduct_notFound() throws Exception {
        Mockito
            .doThrow(NotFoundException.class)
            .when(productsService)
            .deleteProduct(Mockito.anyLong());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .delete("/api/v1/products/1")
                    .header(
                        "Authorization",
                        ControllerTestUtils.createAuthorizationBearer(AuthRoles.DELETE_PRODUCT)
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.NOT_FOUND));
    }
    
    //#endregion

    //#region patchProduct

    /**
     * Parametrized test that checks authorization.
     * When authorizationHeaderValue is null Authorization header is not added
     * 
     * @param expectedStatus
     * @param authorizationHeaderValue
     * @throws Exception
     */
    private void test_patchProduct_authorization(
        HttpStatus expectedStatus,
        String authorizationHeaderValue
    ) throws Exception {
        Mockito
            .doNothing()
            .when(productsService)
            .patchProduct(Mockito.anyLong(), Mockito.any());

        final var patch = new InProductPatch(
            "name",
            "description",
            new BigDecimal(14.99)
        );

        var requestBuilder = MockMvcRequestBuilders
            .patch("/api/v1/products/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(patch));
        if (authorizationHeaderValue != null) {
            requestBuilder = requestBuilder.header("Authorization", authorizationHeaderValue);
        }

        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    /**
     * Parametrized test that checks whether validation returns BAD_REQUEST
     * 
     * @param patch
     * @throws Exception
     */
    private void test_patchProduct_validation(InProductPatch patch) throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders
                    .patch("/api/v1/products/1")
                    .header("Authorization", ControllerTestUtils.createAuthorizationBearer(AuthRoles.UPDATE_PRODUCT))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(patch))
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));

        Mockito
            .verify(productsService, Mockito.never())
            .patchProduct(Mockito.anyLong(), Mockito.any());
    }

    @Test
    public void patchProduct_statusCode204_roleCreateProduct() throws Exception {
        test_patchProduct_authorization(
            HttpStatus.NO_CONTENT,
            ControllerTestUtils.createAuthorizationBearer(AuthRoles.CREATE_PRODUCT)
        );
    }

    @Test
    public void patchProduct_statusCode204_roleUpdateProduct() throws Exception {
        test_patchProduct_authorization(
            HttpStatus.NO_CONTENT,
            ControllerTestUtils.createAuthorizationBearer(AuthRoles.UPDATE_PRODUCT)
        );
    }

    @Test
    public void patchProduct_unauthorized() throws Exception {
        test_patchProduct_authorization(
            HttpStatus.UNAUTHORIZED,
            null
        );
    }

    @Test
    public void patchProduct_forbidden() throws Exception {
        test_patchProduct_authorization(
            HttpStatus.FORBIDDEN,
            ControllerTestUtils.createAuthorizationBearer()
        );
    }

    @Test
    public void patchProduct_nameBlank() throws Exception {
        final var patch = new InProductPatch("", null, null);

        test_patchProduct_validation(patch);
    }

    @Test
    public void patchProduct_descriptionBlank() throws Exception {
        final var patch = new InProductPatch(null, "", null);

        test_patchProduct_validation(patch);
    }

    @Test
    public void patchProduct_priceZero() throws Exception {
        final var patch = new InProductPatch(null, null, BigDecimal.ZERO);

        test_patchProduct_validation(patch);
    }

    @Test
    public void patchProduct_notFound() throws Exception {
        final var patch = new InProductPatch("name", "description", new BigDecimal(24.99));
        
        Mockito
            .doThrow(NotFoundException.class)
            .when(productsService)
            .patchProduct(Mockito.anyLong(), Mockito.any());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .patch("/api/v1/products/1")
                    .header("Authorization", ControllerTestUtils.createAuthorizationBearer(AuthRoles.UPDATE_PRODUCT))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(patch))
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.NOT_FOUND));
    }

    //#endregion
}
