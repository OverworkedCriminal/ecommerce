package ecommerce.dto.addresses;

import lombok.Builder;

@Builder
public record OutAddress(
    Long id,
    String street,
    String house,
    String postalCode,
    String city,
    Long country
) {}