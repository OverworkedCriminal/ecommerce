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
import ecommerce.service.countries.mapper.CountriesMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CountriesService {

    private final CountriesMapper countriesMapper;
    private final CountriesRepository countriesRepository;

    /**
     * Finds country by ID
     * 
     * @param id
     * @return found country
     * @throws NotFoundException country does not exist or it is inactive
     */
    public Country findByIdActive(long id) throws NotFoundException {
        final var country = countriesRepository
            .findByIdAndActiveTrue(id)
            .orElseThrow(() -> NotFoundException.country(id));
        return country;
    }

    /**
     * Find country by id
     * 
     * @param id
     * @return found country
     * @throws NotFoundException country does not exist or is inactive
     */
    public OutCountry getCountry(long id) throws NotFoundException {
        final var countryEntity = findByIdActive(id);
        log.info("found country with id={}", id);

        final var outCountry = countriesMapper.fromEntity(countryEntity);
        return outCountry;
    }

    /**
     * Find all countries (with 'active'=true )
     * 
     * @return found countries
     */
    public List<OutCountry> getCountries() {
        final var countryEntities = countriesRepository.findByActiveTrue();
        log.info("found countries count={}", countryEntities.size());

        final var outCountries = countryEntities.stream()
            .map(countriesMapper::fromEntity)
            .collect(Collectors.toList());

        return outCountries;
    }

    /**
     * Create country
     * 
     * @param inCountry
     * @return created country
     * @throws ConflictException country with such name already exist
     */
    public OutCountry postCountry(InCountry inCountry) throws ConflictException {
        log.trace("{}", inCountry);

        var countryEntity = countriesMapper.intoEntity(inCountry);

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

        final var outCountry = countriesMapper.fromEntity(countryEntity);
        return outCountry;
    }

    /**
     * Delete country by setting 'active' to false
     * 
     * @param id
     * @throws NotFoundException country does not exist or is inactive
     */
    public void deleteCountry(long id) throws NotFoundException {
        final var country = findByIdActive(id);
        log.info("found country with id={}", id);

        country.setActive(false);
        countriesRepository.save(country);
        log.info("deleted country with id={}", id);
    }
}
