package ecommerce.repository.countries;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ecommerce.repository.countries.entity.Country;

public interface CountriesRepository extends JpaRepository<Country, Long>{

    Optional<Country> findByNameAndActiveFalse(String name);
}
