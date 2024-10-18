package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import ecommerce.dto.countries.InCountry;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.repository.countries.CountriesRepository;
import ecommerce.repository.countries.entity.Country;
import ecommerce.service.countries.CountriesService;
import ecommerce.service.countries.mapper.CountriesMapper;

public class CountriesServiceTest {

    private CountriesMapper countriesMapper;
    private CountriesRepository countriesRepository;

    @BeforeEach
    public void setupDependencies() {
        countriesMapper = new CountriesMapper();
        countriesRepository = Mockito.mock(CountriesRepository.class);
    }

    private CountriesService createService() {
        return new CountriesService(countriesMapper, countriesRepository);
    }

    //#region findByIdAndActive

    @Test
    public void findByIdActive_notFound() {
        final Long id = 1L;
        
        Mockito
            .doReturn(Optional.empty())
            .when(countriesRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.findByIdActive(id);
        });
    }

    @Test
    public void findByIdActive() throws NotFoundException {
        final var country = Country.builder()
            .id(1L)
            .active(true)
            .name("name")
            .build();

        Mockito
            .doReturn(Optional.of(country))
            .when(countriesRepository)
            .findByIdAndActiveTrue(Mockito.eq(country.getId()));

        final var service = createService();

        final var out = service.findByIdActive(country.getId());

        assertEquals(country.getId(), out.getId());
        assertEquals(country.getActive(), out.getActive());
        assertEquals(country.getName(), out.getName());
    }
    
    //#endregion

    //#region getCountry

    @Test
    public void getCountry_notFound() {
        final Long id = 1L;
        
        Mockito
            .doReturn(Optional.empty())
            .when(countriesRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.getCountry(id);
        });
    }

    @Test
    public void getCountry() throws NotFoundException {
        final var country = Country.builder()
            .id(1L)
            .active(true)
            .name("name")
            .build();

        Mockito
            .doReturn(Optional.of(country))
            .when(countriesRepository)
            .findByIdAndActiveTrue(country.getId());

        final var service = createService();

        final var out = service.getCountry(country.getId());

        assertEquals(country.getId(), out.id());
        assertEquals(country.getName(), out.name());
    }
    
    //#endregion

    //#region getCountries

    @Test
    public void getCountries() {
        final var countries = List.of(
            Country.builder()
                .id(1L)
                .active(true)
                .name("name 1")
                .build(),
            Country.builder()
                .id(2L)
                .active(true)
                .name("name 2")
                .build()
        );

        Mockito
            .doReturn(countries)
            .when(countriesRepository)
            .findByActiveTrue();

        final var service = createService();

        final var outCountries = service.getCountries();
        
        assertEquals(countries.size(), outCountries.size());
        for (int i = 0; i < countries.size(); ++i) {
            final var country = countries.get(i);
            final var outCountry = outCountries.get(i);
            assertEquals(country.getId(), outCountry.id());
            assertEquals(country.getName(), outCountry.name());
        }
    }

    //#endregion

    //#region postCountry

    @Test
    public void postCountry() throws ConflictException {
        final var inCountry = new InCountry("name");
        final var country = Country.builder()
            .id(1L)
            .active(true)
            .name(inCountry.name())
            .build();

        Mockito
            .doReturn(country)
            .when(countriesRepository)
            .save(Mockito.any());

        final var service = createService();

        final var out = service.postCountry(inCountry);

        assertEquals(country.getId(), out.id());
        assertEquals(country.getName(), out.name());

        Mockito
            .verify(countriesRepository, Mockito.times(1))
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(inCountry.name(), saved.getName());
                })
            );
    }

    @Test
    public void postCountry_nameAlreadyExist() {
        final var inCountry = new InCountry("name");

        Mockito
            .doThrow(DataIntegrityViolationException.class)
            .when(countriesRepository)
            .save(Mockito.any());
        Mockito
            .doReturn(Optional.empty())
            .when(countriesRepository)
            .findByNameAndActiveFalse(Mockito.eq(inCountry.name()));

        final var service = createService();

        assertThrows(ConflictException.class, () -> {
            service.postCountry(inCountry);
        });
    }

    @Test
    public void postCountry_replaceInactiveCountryWithActive() throws ConflictException {
        final var inCountry = new InCountry("name");
        final var country = Country.builder()
            .id(1L)
            .active(false)
            .name(inCountry.name())
            .build();

        Mockito
            .when(countriesRepository.save(Mockito.any()))
            .thenThrow(DataIntegrityViolationException.class)
            .then(AdditionalAnswers.returnsFirstArg());
        Mockito
            .doReturn(Optional.of(country))
            .when(countriesRepository)
            .findByNameAndActiveFalse(Mockito.eq(inCountry.name()));

        final var service = createService();

        final var out = service.postCountry(inCountry);

        assertEquals(country.getId(), out.id());
        assertEquals(country.getName(), out.name());

        final var inOrder = Mockito.inOrder(countriesRepository);
        // Verifying first call does not matter
        inOrder
            .verify(countriesRepository)
            .save(Mockito.any());
        // Verify second call to save updated active to true
        inOrder
            .verify(countriesRepository)
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(country.getId(), saved.getId());
                    assertEquals(true, saved.getActive());
                    assertEquals(country.getName(), saved.getName());
                })
            );
    }
    
    //#endregion

    //#region deleteCountry

    @Test
    public void deleteCountry_notFound() {
        final Long id = 1L;

        Mockito
            .doReturn(Optional.empty())
            .when(countriesRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.deleteCountry(id);
        });
    }

    @Test
    public void deleteCountry() throws NotFoundException {
        final Long id = 1L;
        final var country = Country.builder()
            .id(id)
            .active(true)
            .name("name")
            .build();

        Mockito
            .doReturn(Optional.of(country))
            .when(countriesRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        service.deleteCountry(id);

        Mockito
            .verify(countriesRepository, Mockito.times(1))
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(id, saved.getId());
                    assertEquals(false, saved.getActive());
                    assertEquals(country.getName(), saved.getName());
                })
            );
    }
    
    //#endregion
}