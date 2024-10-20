package ecommerce.service.countries.mapper;

import org.springframework.stereotype.Component;

import ecommerce.dto.countries.InCountry;
import ecommerce.dto.countries.OutCountry;
import ecommerce.repository.countries.entity.Country;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CountriesMapper {

    public Country intoEntity(InCountry country) {
        return Country.builder()
            .active(true)
            .name(country.name())
            .build();
    }

    public OutCountry fromEntity(Country country) {
        return OutCountry.builder()
            .id(country.getId())
            .name(country.getName())
            .build();
    }
}
