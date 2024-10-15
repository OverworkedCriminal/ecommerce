package ecommerce.service.addresses.mapper;

import ecommerce.dto.addresses.InAddress;
import ecommerce.dto.addresses.OutAddress;
import ecommerce.repository.addresses.entity.Address;
import ecommerce.repository.countries.entity.Country;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddressesMapper {

    public Address intoEntity(
        InAddress address,
        Country country
    ) {
        return Address.builder()
            .street(address.street())
            .house(address.house())
            .postalCode(address.postalCode())
            .city(address.city())
            .country(country)
            .build();
    }

    public OutAddress fromEntity(Address address) {
        return OutAddress.builder()
            .id(address.getId())
            .street(address.getStreet())
            .house(address.getHouse())
            .postalCode(address.getPostalCode())
            .city(address.getCity())
            .country(address.getCountry().getId())
            .build();
    }
}
