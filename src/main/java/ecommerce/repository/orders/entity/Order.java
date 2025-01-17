package ecommerce.repository.orders.entity;

import java.time.LocalDateTime;
import java.util.List;

import ecommerce.repository.addresses.entity.Address;
import ecommerce.repository.payments.entity.Payment;
import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Nonnull
    private String username;

    @OneToOne(
        cascade = CascadeType.PERSIST,
        orphanRemoval = true
    )
    @JoinColumn(
        name = "address_id",
        nullable = false
    )
    private Address address;

    @OneToOne(
        cascade = CascadeType.PERSIST
    )
    @JoinColumn(
        name = "payment_id",
        nullable = false
    )
    private Payment payment;

    @Nonnull
    private LocalDateTime orderedAt;
    private LocalDateTime completedAt;

    @OneToMany(
        mappedBy = "order",
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    private List<OrderProduct> orderProducts;
}
