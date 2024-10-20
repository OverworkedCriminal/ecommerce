package ecommerce.service.orders.mapper;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.OutOrder;
import ecommerce.repository.addresses.entity.Address;
import ecommerce.repository.orders.entity.Order;
import ecommerce.repository.payments.entity.Payment;
import ecommerce.service.addresses.mapper.AddressesMapper;
import ecommerce.service.payments.mapper.PaymentsMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrdersMapper {

    private final OrderProductsMapper orderProductsMapper;
    private final AddressesMapper addressesMapper;
    private final PaymentsMapper paymentsMapper;

    public Order intoEntity(
        InOrder order,
        String username,
        Address address,
        Payment payment
    ) {
        return Order.builder()
            .username(username)
            .address(address)
            .payment(payment)
            .orderedAt(LocalDateTime.now())
            .completedAt(null)
            .build();
    }

    public OutOrder fromEntity(Order order) {
        return OutOrder.builder()
            .id(order.getId())
            .username(order.getUsername())
            .address(addressesMapper.fromEntity(order.getAddress()))
            .orderedAt(order.getOrderedAt())
            .completedAt(order.getCompletedAt())
            .payment(paymentsMapper.fromEntity(order.getPayment()))
            .orderProducts(
                order.getOrderProducts()
                    .stream()
                    .map(orderProductsMapper::fromEntity)
                    .collect(Collectors.toList())
            )
            .build();
    }
}
