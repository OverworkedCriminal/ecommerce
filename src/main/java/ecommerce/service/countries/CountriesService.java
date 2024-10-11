package ecommerce.service.countries;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import ecommerce.dto.countries.InCountry;
import ecommerce.dto.countries.OutCountry;
import ecommerce.exception.ConflictException;
import ecommerce.repository.countries.CountriesRepository;
import ecommerce.repository.countries.entity.Country;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CountriesService {

    private final CountriesRepository countriesRepository;

    public OutCountry postProduct(InCountry inCountry) {
        log.trace("{}", inCountry);

        var countryEntity = Country.builder()
            .active(true)
            .name(inCountry.name())
            .build();

        try {
            countryEntity = countriesRepository.save(countryEntity);
            log.info("created country with id={}", countryEntity.getId());

        } catch (DataIntegrityViolationException e) {
            log.info("country with name={} already exist", inCountry.name());

            countryEntity = countriesRepository
                .findByNameAndActiveFalse(inCountry.name())
                .orElseThrow(() -> {
                    return new ConflictException(
                        "country with name=%s and active=true already exist".formatted(inCountry.name())
                    );
                });

            countryEntity.setActive(true);
            countryEntity = countriesRepository.save(countryEntity);
            log.info("updated country with id={}", countryEntity.getId());
        }

        final var outCountry = OutCountry.from(countryEntity);
        return outCountry;
    }
}
