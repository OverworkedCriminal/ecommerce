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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.dto.countries.InCountry;
import ecommerce.dto.countries.OutCountry;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.service.countries.CountriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
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

    @GetMapping("/{id}")
    @Operation(
        summary = "fetch active country by id",
        responses = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "country with such ID does not exist or is inactive")
        }
    )
    public OutCountry getCountry(
        @NotNull @PathVariable Long id
    ) throws NotFoundException {
        return countriesService.getCountry(id);
    }

    @GetMapping("")
    @Operation(
        summary = "fetch all active countries",
        responses = {
            @ApiResponse(responseCode = "200", description = "success")
        }
    )
    public List<OutCountry> getCountries() {
        return countriesService.getCountries();
    }

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
    ) throws ConflictException {
        return countriesService.postCountry(country);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({ AuthRoles.COUNTRY_MANAGE })
    @Operation(
        summary = "delete country by changing active to false",
        security = @SecurityRequirement(name = BEARER),
        responses = {
            @ApiResponse(responseCode = "204", description = "success"),
            @ApiResponse(responseCode = "401", description = "user is unauthenticated"),
            @ApiResponse(responseCode = "403", description = "user lacs any of the roles [" + AuthRoles.COUNTRY_MANAGE + "]"),
            @ApiResponse(responseCode = "404", description = "country does not exist or is not active"),
        }
    )
    public void deleteCountry(
        @NotNull @PathVariable Long id
    ) throws NotFoundException {
        countriesService.deleteCountry(id);
    }
}
