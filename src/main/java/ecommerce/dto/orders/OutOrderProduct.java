package ecommerce.dto.orders;

import ecommerce.dto.products.OutProduct;
import lombok.Builder;

@Builder
public record OutOrderProduct(
    OutProduct product,
    Integer quantity
) {}
