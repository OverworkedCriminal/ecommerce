package ecommerce.controller.v1;

import static ecommerce.configuration.docs.OpenApiConfiguration.BEARER;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.service.categories.CategoriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(
    name = "categories",
    description = "All endpoints responsible for managing orders"
)
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoriesService categoriesService;

    @GetMapping("")
    @Operation(
        summary = "fetch list of all categories",
        responses = {
            @ApiResponse(responseCode = "200", description = "success")
        }
    )
    public List<OutCategory> getCategories() {
        return categoriesService.getCategories();
    }

    @PostMapping("")
    @Secured({ AuthRoles.CATEGORY_MANAGE })
    @Operation(
        summary = "create category",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacks any of the roles [" + AuthRoles.CATEGORY_MANAGE + "]"),
            @ApiResponse(responseCode = "404", description = "parent category does not exist"),
            @ApiResponse(responseCode = "409", description = "category with such name already exist")
        }
    )
    public OutCategory postCategory(
        @Validated @RequestBody InCategory category
    ) throws NotFoundException, ConflictException {
        return categoriesService.postCategory(category);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.CATEGORY_MANAGE })
    @Operation(
        summary = "updates category",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(
                responseCode = "400",
                description = "any of input parameters is invalid or updating category would cause a cycle"
            ),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacks any of the roles [" + AuthRoles.CATEGORY_MANAGE + "]"),
            @ApiResponse(responseCode = "404", description = "category or parent category does not exist"),
            @ApiResponse(responseCode = "409", description = "category with such name already exist")
        }
    )
    public void patchCategory(
        @NotNull @PathVariable Long id,
        @Validated @RequestBody InCategory category
    ) throws NotFoundException, ConflictException, ValidationException {
        categoriesService.putCategory(id, category);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.CATEGORY_MANAGE })
    @Operation(
        summary = "removes category",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacks any of the roles [" + AuthRoles.CATEGORY_MANAGE + "]"),
            @ApiResponse(responseCode = "404", description = "category does not exist"),
            @ApiResponse(responseCode = "409", description = "category cannot be removed because it's assigned to some product")
        }
    )
    public void deleteCategory(
        @NotNull @PathVariable Long id
    ) throws NotFoundException, ConflictException {
        categoriesService.deleteCategory(id);
    }
}
