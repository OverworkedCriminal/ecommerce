package ecommerce.repository.orders.entity;

import java.math.BigDecimal;

import ecommerce.repository.products.entity.Product;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(
        fetch = FetchType.EAGER,
        optional = false
    )
    @JoinColumn(
        name = "product_id",
        nullable = false,
        updatable = false
    )
    private Product product;

    @ManyToOne
    @JoinColumn(
        name = "order_id",
        nullable = false,
        updatable = false
    )
    private Order order;

    @Nonnull
    private BigDecimal price;
    @Nonnull
    private Integer quantity;
}
