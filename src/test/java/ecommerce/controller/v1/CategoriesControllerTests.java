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

    //#region postCategory

    private void test_postCategory_authorization(
        HttpStatus expectedStatus,
        @Nullable RequestPostProcessor postProcessor
    ) throws Exception {
        final var category = new InCategory("name");

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
                .authorities(new SimpleGrantedAuthority(AuthRoles.MANAGE_CATEGORY))
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
                            .authorities(new SimpleGrantedAuthority(AuthRoles.MANAGE_CATEGORY))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void postCategory_nullName() throws Exception {
        final var category = new InCategory(null);
        test_postCategory_validation(category);
    }

    @Test
    public void postCategory_emptyName() throws Exception {
        final var category = new InCategory("");
        test_postCategory_validation(category);
    }

    @Test
    public void postCategory_conflictException() throws Exception {
        final var category = new InCategory("name");
        
        Mockito
            .doThrow(ConflictException.class)
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
                            .authorities(new SimpleGrantedAuthority(AuthRoles.MANAGE_CATEGORY))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.CONFLICT));
    }

    //#endregion

    //#region putProduct

    private void test_putCategory_authorization(
        HttpStatus expectedStatus,
        @Nullable RequestPostProcessor postProcessor
    ) throws Exception {
        final var category = new InCategory("name");

        var requestBuilder = MockMvcRequestBuilders
            .put("/api/v1/categories/1")
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
    public void putCategory_statusCode204() throws Exception {
        test_putCategory_authorization(
            HttpStatus.NO_CONTENT,
            SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority(AuthRoles.MANAGE_CATEGORY))
        );
    }

    @Test
    public void putCategory_unauthorized() throws Exception {
        test_putCategory_authorization(
            HttpStatus.UNAUTHORIZED, 
            null
        );
    }

    @Test
    public void putCategory_forbidden() throws Exception {
        test_putCategory_authorization(
            HttpStatus.FORBIDDEN, 
            SecurityMockMvcRequestPostProcessors.jwt()
        );
    }

    private void test_putCategory_validation(
        InCategory category
    ) throws Exception {
        mvc
            .perform(
                MockMvcRequestBuilders
                    .put("/api/v1/categories/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(category))
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.MANAGE_CATEGORY))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void putCategory_nullName() throws Exception {
        final var category = new InCategory(null);
        test_putCategory_validation(category);
    }

    @Test
    public void putCategory_emptyName() throws Exception {
        final var category = new InCategory("");
        test_putCategory_validation(category);
    }

    @Test
    public void putCategory_conflictException() throws Exception {
        final var category = new InCategory("name");
        
        Mockito
            .doThrow(ConflictException.class)
            .when(categoriesService)
            .putCategory(Mockito.anyLong(), Mockito.any());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .put("/api/v1/categories/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(category))
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.MANAGE_CATEGORY))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.CONFLICT));
    }

    @Test
    public void putCategory_notFoundException() throws Exception {
        final var category = new InCategory("name");

        Mockito
            .doThrow(NotFoundException.class)
            .when(categoriesService)
            .putCategory(Mockito.anyLong(), Mockito.any());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .put("/api/v1/categories/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(category))
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.MANAGE_CATEGORY))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.NOT_FOUND));
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
                .authorities(new SimpleGrantedAuthority(AuthRoles.MANAGE_CATEGORY))
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

    @Test
    public void deleteCategory_notFoundException() throws Exception {
        Mockito
            .doThrow(NotFoundException.class)
            .when(categoriesService)
            .deleteCategory(Mockito.anyLong());

        mvc
            .perform(
                MockMvcRequestBuilders
                    .delete("/api/v1/categories/1")
                    .with(
                        SecurityMockMvcRequestPostProcessors
                            .jwt()
                            .authorities(new SimpleGrantedAuthority(AuthRoles.MANAGE_CATEGORY))
                    )
            )
            .andExpect(ControllerTestUtils.expectStatus(HttpStatus.NOT_FOUND));
    }
    
    //#endregion
}