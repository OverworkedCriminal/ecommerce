package ecommerce.service.countries;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import ecommerce.dto.countries.InCountry;
import ecommerce.dto.countries.OutCountry;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.repository.countries.CountriesRepository;
import ecommerce.repository.countries.entity.Country;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CountriesService {

    private final CountriesRepository countriesRepository;

    public OutCountry getCountry(long id) {
        final var countryEntity = countriesRepository
            .findByIdAndActiveTrue(id)
            .orElseThrow(() -> NotFoundException.country(id));

        final var outCountry = OutCountry.from(countryEntity);
        return outCountry;
    }

    public List<OutCountry> getCountries() {
        final var countryEntities = countriesRepository.findByActiveTrue();
        log.info("found countries count={}", countryEntities.size());

        final var outCountries = countryEntities.stream()
            .map(OutCountry::from)
            .collect(Collectors.toList());

        return outCountries;
    }

    public OutCountry postCountry(InCountry inCountry) {
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

    public void deleteCountry(long id) {
        final var country = countriesRepository
            .findByIdAndActiveTrue(id)
            .orElseThrow(() -> NotFoundException.country(id));

        country.setActive(false);
        countriesRepository.save(country);
        log.info("deleted country with id={}", id);
    }
}
