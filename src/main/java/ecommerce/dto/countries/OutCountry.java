package ecommerce.dto.countries;

import lombok.Builder;

@Builder
public record OutCountry(
    Long id,
    String name
) {}
