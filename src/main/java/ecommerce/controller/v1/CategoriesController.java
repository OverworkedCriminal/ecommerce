package ecommerce.controller.v1;

import static ecommerce.configuration.docs.OpenApiConfiguration.BEARER;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.dto.categories.InCategory;
import ecommerce.dto.categories.OutCategory;
import ecommerce.service.categories.ICategoriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(
    name = "categories",
    description = "All endpoints related to managing orders"
)
@RequiredArgsConstructor
public class CategoriesController {

    private final ICategoriesService categoriesService;

    @PostMapping("")
    @Secured({ AuthRoles.MANAGE_CATEGORY })
    @Operation(
        summary = "create category",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacks any of the roles [" + AuthRoles.MANAGE_CATEGORY + "]"),
            @ApiResponse(responseCode = "409", description = "category with such name already exist")
        }
    )
    public OutCategory postCategory(
        @Validated @RequestBody InCategory category
    ) {
        return categoriesService.postCategory(category);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.MANAGE_CATEGORY })
    @Operation(
        summary = "updates category",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacks any of the roles [" + AuthRoles.MANAGE_CATEGORY + "]"),
            @ApiResponse(responseCode = "404", description = "category does not exist"),
            @ApiResponse(responseCode = "409", description = "category with such name already exist")
        }
    )
    public void putCategory(
        @PathVariable long id,
        @Validated @RequestBody InCategory category
    ) {
        categoriesService.putCategory(id, category);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.MANAGE_CATEGORY })
    @Operation(
        summary = "removes category",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacks any of the roles [" + AuthRoles.MANAGE_CATEGORY + "]"),
            @ApiResponse(responseCode = "404", description = "category does not exist"),
        }
    )
    public void deleteCategory(
        @PathVariable long id
    ) {
        categoriesService.deleteCategory(id);
    }
}
