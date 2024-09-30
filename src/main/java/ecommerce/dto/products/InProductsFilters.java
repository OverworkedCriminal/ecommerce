package ecommerce.dto.products;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import ecommerce.dto.validation.nullablenotblank.NullableNotBlank;
import ecommerce.repository.products.entity.Product;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.DecimalMin;

public record InProductsFilters(
    @Nullable @NullableNotBlank String name,
    @Nullable @DecimalMin(value = "0", inclusive = true) BigDecimal minPrice,
    @Nullable @DecimalMin(value = "0", inclusive = true) BigDecimal maxPrice
) {

    public Specification<Product> intoSpecification() {
        return (root, query, cb) -> {
            final var predicates = new ArrayList<Predicate>();

            if (this.name != null) {
                final Path<String> path = root.get("name");
                final Predicate predicate = cb.like(
                    cb.upper(path),
                    name.toUpperCase()
                );
                predicates.add(predicate);
            }
            if (this.minPrice != null) {
                final Path<BigDecimal> path = root.get("price");
                final Predicate predicate = cb.greaterThanOrEqualTo(path, minPrice);
                predicates.add(predicate);
            }
            if (this.maxPrice != null) {
                final Path<BigDecimal> path = root.get("price");
                final Predicate predicate = cb.lessThanOrEqualTo(path, maxPrice);
                predicates.add(predicate);
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            } else {
                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

}