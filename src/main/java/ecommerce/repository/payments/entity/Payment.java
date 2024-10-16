package ecommerce.repository.payments.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ecommerce.repository.orders.entity.Order;
import ecommerce.repository.paymentmethods.entity.PaymentMethod;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(
        name = "payment_method_id",
        nullable = false
    )
    private PaymentMethod paymentMethod;

    @Nonnull
    @Column(precision = 2)
    private BigDecimal amount;

    private LocalDateTime completedAt;

    @OneToOne(mappedBy = "payment")
    private Order order;
}
