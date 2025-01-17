package ecommerce.controller.v1;

import static ecommerce.configuration.docs.OpenApiConfiguration.BEARER;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.dto.addresses.InAddress;
import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.InOrderCompletedAtUpdate;
import ecommerce.dto.orders.InOrderFilters;
import ecommerce.dto.orders.OutOrder;
import ecommerce.dto.payments.InPaymentCompletedAtUpdate;
import ecommerce.dto.shared.InPagination;
import ecommerce.dto.shared.OutPage;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.service.orders.OrdersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(
    name = "orders",
    description = "All endpoints responsible for managing orders"
)
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    @GetMapping("")
    @Operation(
        summary = "fetch orders",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid")
        }
    )
    public OutPage<OutOrder> getOrders(
        @Validated @ModelAttribute InPagination pagination,
        @Validated @ModelAttribute InOrderFilters filters
    ) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        return ordersService.getOrders(auth, filters, pagination);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "fetch order by id",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "order does not exist or does not belong to the user")
        }
    )
    public OutOrder getOrder(
        @NotNull @PathVariable Long id
    ) throws NotFoundException {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        return ordersService.getOrder(auth, id);
    }

    @PostMapping("")
    @Operation(
        summary = "create order",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(
                responseCode = "400",
                description = "any of input parameters is invalid or order contains duplicated product"
            ),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(
                responseCode = "404",
                description = "country, payment method or any of ordered products does not exist"
            )
        }
    )
    public OutOrder postOrder(
        @Validated @RequestBody InOrder order
    ) throws NotFoundException, ValidationException {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        return ordersService.postOrder(auth, order);
    }

    @PutMapping("/{id}/address")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "updates address of the order",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(
                responseCode = "404",
                description = "order or does not exist or order does not belong to the user"
            ),
            @ApiResponse(responseCode = "409", description = "order has already been completed")
        }
    )
    public void putOrderAddress(
        @NotNull @PathVariable Long id,
        @Validated @RequestBody InAddress address
    ) throws NotFoundException, ConflictException {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        ordersService.putOrderAddress(auth, id, address);
    }

    @PutMapping("/{id}/completed-at")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.ORDER_UPDATE_COMPLETED_AT, AuthRoles.ORDER_UPDATE })
    @Operation(
        summary = "marks order as completed by updating completedAt",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(
                responseCode = "403",
                description = "user lacks any of the roles [" + AuthRoles.ORDER_UPDATE_COMPLETED_AT + "," + AuthRoles.ORDER_UPDATE + "]"
            ),
            @ApiResponse(responseCode = "404", description = "order does not exist"),
            @ApiResponse(responseCode = "409", description = "order has already been completed")
        }
    )
    public void putOrderCompletedAt(
        @NotNull @PathVariable Long id,
        @Validated @RequestBody InOrderCompletedAtUpdate update
    ) throws NotFoundException, ConflictException, ValidationException {
        ordersService.putOrderCompletedAt(id, update);
    }

    @PutMapping("/{id}/payment/completed-at")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.ORDER_UPDATE_COMPLETED_AT, AuthRoles.ORDER_UPDATE })
    @Operation(
        summary = "marks order payment as completed by updating completedAt",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(
                responseCode = "403",
                description = "user lacks any of the roles [" + AuthRoles.ORDER_UPDATE_COMPLETED_AT + "," + AuthRoles.ORDER_UPDATE + "]"
            ),
            @ApiResponse(responseCode = "404", description = "order does not exist"),
            @ApiResponse(responseCode = "409", description = "order payment has already been completed")
        }
    )
    public void putOrderPaymentCompletedAt(
        @NotNull @PathVariable Long id,
        @Validated @RequestBody InPaymentCompletedAtUpdate update
    ) throws NotFoundException, ConflictException, ValidationException {
        ordersService.putOrderPaymentCompletedAt(id, update);
    }

}
