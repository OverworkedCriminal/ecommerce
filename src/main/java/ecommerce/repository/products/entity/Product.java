package ecommerce.repository.products.entity;

import java.math.BigDecimal;
import java.util.List;

import ecommerce.repository.categories.entity.Category;
import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    /**
     * when false, product was deleted
     */
    @Nonnull
    private boolean active;

    @Nonnull
    private String name;
    @Nonnull
    private String description;

    @Nonnull
    @Column(scale = 2)
    private BigDecimal price;

    @ManyToMany(
        fetch = FetchType.EAGER,
        cascade = {
            CascadeType.PERSIST
        }
    )
    @JoinTable(
        name = "products_categories",
        joinColumns = @JoinColumn(
            name = "product_id",
            nullable = false
        ),
        inverseJoinColumns = @JoinColumn(
            name = "category_id",
            nullable = false
        )
    )
    private List<Category> categories;
}
