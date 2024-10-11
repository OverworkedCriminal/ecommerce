package ecommerce.controller.v1;

import static ecommerce.configuration.docs.OpenApiConfiguration.BEARER;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.dto.countries.InCountry;
import ecommerce.dto.countries.OutCountry;
import ecommerce.service.countries.CountriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/countries")
@Tag(
    name = "countries",
    description = "All endpoints responsible for managing countries"
)
@RequiredArgsConstructor
public class CountriesController {

    private final CountriesService countriesService;

    @PostMapping("")
    @Secured({ AuthRoles.COUNTRY_MANAGE })
    @Operation(
        summary = "create country",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "400", description = "any of input parameters is invalid"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacs any of the roles [" + AuthRoles.COUNTRY_MANAGE + "]"),
            @ApiResponse(responseCode = "409", description = "country with such name already exist")
        }
    )
    public OutCountry postCountry(
        @Validated @RequestBody InCountry country
    ) {
        return countriesService.postProduct(country);
    }
}
