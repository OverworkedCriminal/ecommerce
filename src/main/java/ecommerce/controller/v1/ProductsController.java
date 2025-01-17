package ecommerce.controller.v1;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.dto.products.InProduct;
import ecommerce.dto.products.InProductPatch;
import ecommerce.dto.products.InProductFilters;
import ecommerce.dto.products.OutProduct;
import ecommerce.dto.products.OutProductDetails;
import ecommerce.dto.shared.InPagination;
import ecommerce.dto.shared.OutPage;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.service.products.ProductsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import static ecommerce.configuration.docs.OpenApiConfiguration.BEARER;

@RestController
@RequestMapping("/api/v1/products")
@Tag(
    name = "products",
    description = "All endpoints responsible for managing products"
)
@RequiredArgsConstructor
public class ProductsController {

    private final ProductsService productsService;

    @GetMapping("/{id}")
    @Operation(
        summary = "fetch single active product",
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "product does not exist")
        }
    )
    public OutProductDetails getProduct(
        @NotNull @PathVariable Long id
    ) throws NotFoundException {
        return productsService.getProduct(id);
    }

    @GetMapping("")
    @Operation(
        summary = "fetch page of active products",
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid")
        }
    )
    public OutPage<OutProduct> getProducts(
        @Validated @ModelAttribute InPagination pagination,
        @Validated @ModelAttribute InProductFilters filters
    ) {
        return productsService.getProducts(filters, pagination);
    }

    @PostMapping("")
    @Secured({ AuthRoles.PRODUCT_CREATE })
    @Operation(
        summary = "create product",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacks role " + AuthRoles.PRODUCT_CREATE),
            @ApiResponse(responseCode = "404", description = "category does not exist")
        }
    )
    public OutProductDetails postProduct(
        @Validated @RequestBody InProduct product
    ) throws NotFoundException, ValidationException {
        return productsService.postProduct(product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.PRODUCT_DELETE })
    @Operation(
        summary = "delete product (mark it as inactive)",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacks role " + AuthRoles.PRODUCT_DELETE),
            @ApiResponse(responseCode = "404", description = "product does not exist")
        }
    )
    public void deleteProduct(
        @NotNull @PathVariable Long id
    ) throws NotFoundException {
        productsService.deleteProduct(id);
    }

    @PatchMapping("/{id}")
    @Secured({ AuthRoles.PRODUCT_CREATE, AuthRoles.PRODUCT_UPDATE })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "update part of the product",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(
                responseCode = "403",
                description = "user lacks any of the roles [" + AuthRoles.PRODUCT_CREATE + ", " + AuthRoles.PRODUCT_UPDATE + "]"),
            @ApiResponse(responseCode = "404", description = "product or category does not exist")
        }
    )
    public void patchProduct(
        @NotNull @PathVariable Long id,
        @Validated @RequestBody InProductPatch productPatch
    ) throws NotFoundException, ValidationException {
        productsService.patchProduct(id, productPatch);
    }
}
