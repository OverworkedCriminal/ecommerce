package ecommerce.controller.v1;

import static ecommerce.configuration.docs.OpenApiConfiguration.BEARER;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.dto.paymentmethods.InPaymentMethod;
import ecommerce.dto.paymentmethods.InPaymentMethodPatch;
import ecommerce.dto.paymentmethods.OutPaymentMethod;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.service.paymentmethods.PaymentMethodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payment-methods")
@Tag(
    name = "payment methods",
    description = "all endpoints responsible for managing payment methods"
)
@RequiredArgsConstructor
public class PaymentMethodsController {

    private final PaymentMethodsService paymentMethodsService;

    @GetMapping("")
    @Operation(
        summary = "fetch all available payment methods",
        responses = {
            @ApiResponse(responseCode = "200", description = "success")
        }
    )
    public List<OutPaymentMethod> getPaymentMethods() {
        return paymentMethodsService.getPaymentMethods();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "fetch payment method by id",
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of the input parameters is invalid"),
            @ApiResponse(responseCode = "404", description = "payment method does not exist or was removed")
        }
    )
    public OutPaymentMethod getPaymentMethod(
        @NotNull @PathVariable Long id
    ) throws NotFoundException {
        return paymentMethodsService.getPaymentMethod(id);
    }

    @PostMapping("")
    @Secured({ AuthRoles.PAYMENT_METHOD_MANAGE })
    @Operation(
        summary = "create payment method",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of the input parameteres is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacks any of the roles [" + "]")
        }
    )
    public OutPaymentMethod postPaymentMethod(
        @Validated @RequestBody InPaymentMethod paymentMethod
    ) throws ValidationException {
        return paymentMethodsService.postPaymentMethod(paymentMethod);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.PAYMENT_METHOD_MANAGE })
    @Operation(
        summary = "patch payment method",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacks any of the roles [" + "]"),
            @ApiResponse(responseCode = "404", description = "payment method does not exist or was removed")
        }
    )
    public void patchPaymentMethod(
        @NotNull @PathVariable Long id,
        @Validated @RequestBody InPaymentMethodPatch paymentMethodPatch
    ) throws NotFoundException, ValidationException {
        paymentMethodsService.patchPaymentMethod(id, paymentMethodPatch);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.PAYMENT_METHOD_MANAGE })
    @Operation(
        summary = "delete payment method",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacks any of the roles [" + "]"),
            @ApiResponse(responseCode = "404", description = "payment method does not exist or was removed")
        }
    ) 
    public void deletePaymentMethod(
        @NotNull @PathVariable Long id
    ) throws NotFoundException {
        paymentMethodsService.deletePaymentMethod(id);
    }
}
