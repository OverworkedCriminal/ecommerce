package ecommerce.dto.countries;

import ecommerce.repository.countries.entity.Country;
import lombok.Builder;

@Builder
public record OutCountry(
    Long id,
    String name
) {

    public static OutCountry from(Country country) {
        return OutCountry.builder()
            .id(country.getId())
            .name(country.getName())
            .build();
    }
}
