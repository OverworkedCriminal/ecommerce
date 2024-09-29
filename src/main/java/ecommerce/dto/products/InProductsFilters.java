package ecommerce.dto.products;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.jpa.domain.Specification;

import ecommerce.dto.validation.optionalnotblank.OptionalNotBlank;
import ecommerce.repository.products.entity.Product;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public record InProductsFilters(
    @OptionalNotBlank Optional<String> name
) {

    public Specification<Product> intoSpecification() {
        return (root, query, cb) -> {
            final var predicates = Stream.of(
                this.name.map(name -> {
                    final Path<String> path = root.get("name");
                    final Predicate predicate = cb
                        .like(
                            cb.upper(path),
                            name.toUpperCase()
                        );

                    return predicate;
                })
            )
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

            if (predicates.isEmpty()) {
                return cb.conjunction();
            } else {
                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

}