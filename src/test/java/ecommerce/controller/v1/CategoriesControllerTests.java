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
import ecommerce.dto.categories.InCategory;
import ecommerce.dto.categories.InCategoryPatch;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.service.categories.ICategoriesService;
import jakarta.annotation.Nullable;

@WebMvcTest(CategoriesController.class)
@Import(JwtAuthConfiguration.class)
public class CategoriesControllerTests {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ICategoriesService categoriesService;

    //#region getCategories

    @Test
    public void getCategories_statusCode200() throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/categories")
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.OK));
    }

    //#endregion

    //#region postCategory

    private void test_postCategory_authorization(
        HttpStatus expectedStatus,
        @Nullable RequestPostProcessor postProcessor
    ) throws Exception {
        final var category = new InCategory(
            "name",
            null
        );

        var requestBuilder = MockMvcRequestBuilders
            .post("/api/v1/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(category));
        if (postProcessor != null) {
            requestBuilder = requestBuilder.with(postProcessor);
        }

        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void postCategory_statusCode200() throws Exception {
        test_postCategory_authorization(
            HttpStatus.OK,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.CATEGORY_MANAGE))
        );
    }

    @Test
    public void postCategory_unauthorized() throws Exception {
        test_postCategory_authorization(
            HttpStatus.UNAUTHORIZED, 
            null
        );
    }

    @Test
    public void postCategory_forbidden() throws Exception {
        test_postCategory_authorization(
            HttpStatus.FORBIDDEN, 
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    private void test_postCategory_validation(
        InCategory category
    ) throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(category))
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.CATEGORY_MANAGE))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void postCategory_nullName() throws Exception {
        final var category = new InCategory(
            null,
            null
        );
        test_postCategory_validation(category);
    }

    @Test
    public void postCategory_emptyName() throws Exception {
        final var category = new InCategory(
            "",
            null
        );
        test_postCategory_validation(category);
    }

    private void test_postCategory_exceptionHandling(
        HttpStatus expectedStatus,
        Class<? extends Exception> exceptionClass
    ) throws Exception {
        final var category = new InCategory(
            "name",
            null
        );

        Mockito
            .doThrow(exceptionClass)
            .when(categoriesService)
            .postCategory(Mockito.any());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(category))
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.CATEGORY_MANAGE))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void postCategory_conflictException() throws Exception {
        test_postCategory_exceptionHandling(HttpStatus.CONFLICT, ConflictException.class);
    }

    @Test
    public void postCategory_notFoundException() throws Exception {
        test_postCategory_exceptionHandling(HttpStatus.NOT_FOUND, NotFoundException.class);
    }

    //#endregion

    //#region patchCategory

    private void test_patchCategory_authorization(
        HttpStatus expectedStatus,
        @Nullable RequestPostProcessor postProcessor
    ) throws Exception {
        final var category = new InCategoryPatch(
            "name",
            null
        );

        var requestBuilder = MockMvcRequestBuilders
            .patch("/api/v1/categories/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(category));
        if (postProcessor != null) {
            requestBuilder = requestBuilder.with(postProcessor);
        }

        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void patchCategory_statusCode204() throws Exception {
        test_patchCategory_authorization(
            HttpStatus.NO_CONTENT,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.CATEGORY_MANAGE))
        );
    }

    @Test
    public void patchCategory_unauthorized() throws Exception {
        test_patchCategory_authorization(
            HttpStatus.UNAUTHORIZED, 
            null
        );
    }

    @Test
    public void patchCategory_forbidden() throws Exception {
        test_patchCategory_authorization(
            HttpStatus.FORBIDDEN, 
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    private void test_patchCategory_validation(
        InCategoryPatch category
    ) throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders
                    .patch("/api/v1/categories/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(category))
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.CATEGORY_MANAGE))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void patchCategory_emptyName() throws Exception {
        final var category = new InCategoryPatch(
            "",
            null
        );
        test_patchCategory_validation(category);
    }

    private void test_patchCategory_exceptionHandling(
        HttpStatus expectedStatus,
        Class<? extends Exception> exceptionClass
    ) throws Exception {
        final var category = new InCategory(
            "name",
            null
        );

        Mockito
            .doThrow(exceptionClass)
            .when(categoriesService)
            .patchCategory(Mockito.anyLong(), Mockito.any());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .patch("/api/v1/categories/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(category))
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.CATEGORY_MANAGE))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void patchCategory_conflictException() throws Exception {
        test_patchCategory_exceptionHandling(HttpStatus.CONFLICT, ConflictException.class);
    }

    @Test
    public void patchCategory_notFoundException() throws Exception {
        test_patchCategory_exceptionHandling(HttpStatus.NOT_FOUND, NotFoundException.class);
    }

    //#endregion

    //#region deleteCategory

    private void test_deleteCategory_authorization(
        HttpStatus expectedStatus,
        @Nullable RequestPostProcessor postProcessor
    ) throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/api/v1/categories/1");
        if (postProcessor != null) {
            requestBuilder = requestBuilder.with(postProcessor);
        }

        mvc
            .perform(requestBuilder)
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void deleteCategory_statusCode204() throws Exception {
        test_deleteCategory_authorization(
            HttpStatus.NO_CONTENT,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.CATEGORY_MANAGE))
        );
    }

    @Test
    public void deleteCategory_unauthorized() throws Exception {
        test_deleteCategory_authorization(
            HttpStatus.UNAUTHORIZED, 
            null
        );
    }

    @Test
    public void deleteCategory_forbidden() throws Exception {
        test_deleteCategory_authorization(
            HttpStatus.FORBIDDEN, 
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    private void test_deleteCategory_exceptionHandling(
        HttpStatus expectedStatus,
        Class<? extends Exception> exceptionClass
    ) throws Exception {
        Mockito
            .doThrow(exceptionClass)
            .when(categoriesService)
            .deleteCategory(Mockito.anyLong());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .delete("/api/v1/categories/1")
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.CATEGORY_MANAGE))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(expectedStatus));
    }

    @Test
    public void deleteCategory_notFoundException() throws Exception {
        test_deleteCategory_exceptionHandling(HttpStatus.NOT_FOUND, NotFoundException.class);
    }

    @Test
    public void deleteCategory_conflictException() throws Exception {
        test_deleteCategory_exceptionHandling(HttpStatus.CONFLICT, ConflictException.class);
    }
    
    //#endregion
}
