package ecommerce.dto.orders;

import java.time.LocalDateTime;
import java.util.List;

import ecommerce.dto.addresses.OutAddress;
import ecommerce.dto.payments.OutPayment;
import lombok.Builder;

@Builder
public record OutOrder(
    Long id,
    String username,
    OutAddress address,
    LocalDateTime orderedAt,
    LocalDateTime completedAt,
    OutPayment payment,
    List<OutOrderProduct> orderProducts
) {}
