package ecommerce.repository.addresses;

import org.springframework.data.jpa.repository.JpaRepository;

import ecommerce.repository.addresses.entity.Address;

public interface AddressesRepository extends JpaRepository<Address, Long> {

}
