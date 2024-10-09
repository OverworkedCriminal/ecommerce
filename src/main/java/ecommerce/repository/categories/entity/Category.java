package ecommerce.repository.categories.entity;

import java.util.List;

import ecommerce.repository.products.entity.Product;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "categories",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "name" })
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Nonnull
    private String name;

    @ManyToOne
    @JoinColumn(
        name = "parent_category_id",
        nullable = true
    )
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory")
    private List<Category> childCategories;

    @OneToMany(mappedBy = "category")
    List<Product> products;
}
