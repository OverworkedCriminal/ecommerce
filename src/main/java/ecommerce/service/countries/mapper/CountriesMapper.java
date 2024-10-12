package ecommerce.service.countries.mapper;

import ecommerce.dto.countries.InCountry;
import ecommerce.dto.countries.OutCountry;
import ecommerce.repository.countries.entity.Country;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CountriesMapper {

    public static Country intoEntity(InCountry country) {
        return Country.builder()
            .active(true)
            .name(country.name())
            .build();
    }

    public static OutCountry fromEntity(Country country) {
        return OutCountry.builder()
            .id(country.getId())
            .name(country.getName())
            .build();
    }
}
