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
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductsController {

    private final ProductsService productsService;

    @GetMapping("")
    public OutPage<OutProduct> getProducts(
        @Validated @ModelAttribute InPagination pagination,
        @Validated @ModelAttribute InProductsFilters filters
    ) {
        return productsService.getProducts(filters, pagination);
    }

    @PostMapping("")
    @Secured({ AuthRoles.CREATE_PRODUCT })
    public OutProduct postProduct(
        @Validated @RequestBody InProduct product
    ) {
        return productsService.postProduct(product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.DELETE_PRODUCT })
    public void deleteProduct(
        @PathVariable long id
    ) {
        productsService.deleteProduct(id);
    }

    @PatchMapping("/{id}")
    @Secured({ AuthRoles.CREATE_PRODUCT, AuthRoles.UPDATE_PRODUCT })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchProduct(
        @PathVariable long id,
        @Validated @RequestBody InProductPatch productPatch
    ) {
        productsService.patchProduct(id, productPatch);
    }
}
