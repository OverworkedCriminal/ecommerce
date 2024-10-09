package ecommerce.repository.orders.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @Nonnull
    private LocalDateTime orderedAt;
    private LocalDateTime completedAt;

    @Nonnull
    private BigDecimal price;

    @OneToMany(
        mappedBy = "order",
        fetch = FetchType.EAGER,
        orphanRemoval = true,
        cascade = {
            CascadeType.PERSIST,
        }
    )
    private List<OrderProduct> orderProducts;
}
