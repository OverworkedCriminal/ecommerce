package ecommerce.dto.orders;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;

@Validated
public record InOrderProduct(
    long productId,
    @Min(1) int quantity
) {}
