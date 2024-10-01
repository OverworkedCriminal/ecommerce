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
import ecommerce.dto.products.InProductsFilters;
import ecommerce.dto.products.OutProduct;
import ecommerce.dto.shared.InPagination;
import ecommerce.dto.shared.OutPage;
import ecommerce.service.products.ProductsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import static ecommerce.configuration.docs.OpenApiConfiguration.BEARER;

@RestController
@RequestMapping("/api/v1/products")
@Tag(
    name = "products",
    description = "All endpoints related to managing products"
)
@RequiredArgsConstructor
public class ProductsController {

    private final ProductsService productsService;

    @GetMapping("")
    @Operation(summary = "fetch page of active products")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "success"),
        @ApiResponse(responseCode = "400", description = "any of input parameters is invalid")
    })
    public OutPage<OutProduct> getProducts(
        @Validated @ModelAttribute InPagination pagination,
        @Validated @ModelAttribute InProductsFilters filters
    ) {
        return productsService.getProducts(filters, pagination);
    }

    @PostMapping("")
    @Secured({ AuthRoles.CREATE_PRODUCT })
    @SecurityRequirement(name = BEARER)
    @Operation(summary = "create product")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "success"),
        @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
        @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
        @ApiResponse(responseCode = "403", description = "user lacks role " + AuthRoles.CREATE_PRODUCT)
    })
    public OutProduct postProduct(
        @Validated @RequestBody InProduct product
    ) {
        return productsService.postProduct(product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.DELETE_PRODUCT })
    @SecurityRequirement(name = BEARER)
    @Operation(summary = "delete product (mark it as inactive)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "success"),
        @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
        @ApiResponse(responseCode = "403", description = "user lacks role " + AuthRoles.DELETE_PRODUCT),
        @ApiResponse(responseCode = "404", description = "product does not exist")
    })
    public void deleteProduct(
        @PathVariable long id
    ) {
        productsService.deleteProduct(id);
    }

    @PatchMapping("/{id}")
    @Secured({ AuthRoles.CREATE_PRODUCT, AuthRoles.UPDATE_PRODUCT })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = BEARER)
    @Operation(summary = "update part of the product")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "success"),
        @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
        @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
        @ApiResponse(
            responseCode = "403",
            description = "user lacks any of the roles [" + AuthRoles.CREATE_PRODUCT + ", " + AuthRoles.UPDATE_PRODUCT + "]"),
        @ApiResponse(responseCode = "404", description = "product does not exist")
    })
    public void patchProduct(
        @PathVariable long id,
        @Validated @RequestBody InProductPatch productPatch
    ) {
        productsService.patchProduct(id, productPatch);
    }
}
