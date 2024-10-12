package ecommerce.dto.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import ecommerce.dto.addresses.OutAddress;
import lombok.Builder;

@Builder
public record OutOrder(
    Long id,
    String username,
    OutAddress address,
    LocalDateTime orderedAt,
    LocalDateTime completedAt,
    BigDecimal price,
    List<OutOrderProduct> orderProducts
) {}
