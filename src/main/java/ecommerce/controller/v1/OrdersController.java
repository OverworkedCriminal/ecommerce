package ecommerce.controller.v1;

import static ecommerce.configuration.docs.OpenApiConfiguration.BEARER;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.InOrderCompletedAtUpdate;
import ecommerce.dto.orders.OutOrder;
import ecommerce.service.orders.IOrdersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(
    name = "orders",
    description = "All endpoints responsible for managing orders"
)
@RequiredArgsConstructor
public class OrdersController {

    private final IOrdersService ordersService;

    @PostMapping("")
    @Operation(
        summary = "create order",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated")
        }
    )
    public OutOrder postOrder(
        @Validated @RequestBody InOrder order
    ) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        return ordersService.postOrder(auth, order);
    }

    @PutMapping("/{id}/completed-at")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.UPDATE_ORDER_COMPLETED_AT })
    @Operation(
        summary = "marks order as completed by updating completedAt",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacks any of the roles [" + AuthRoles.UPDATE_ORDER_COMPLETED_AT + "]"),
            @ApiResponse(responseCode = "409", description = "order have already been completed")
        }
    )
    public void putOrderCompletedAt(
        @PathVariable long id,
        @Validated @RequestBody InOrderCompletedAtUpdate update
    ) {
        ordersService.putOrderCompletedAt(id, update);
    }

}
