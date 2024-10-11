package ecommerce.dto.addresses;

import ecommerce.repository.addresses.entity.Address;
import lombok.Builder;

@Builder
public record OutAddress(
    Long id,
    String street,
    String house,
    String postalCode,
    String city,
    Long country
) {

    public static OutAddress from(Address address) {
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