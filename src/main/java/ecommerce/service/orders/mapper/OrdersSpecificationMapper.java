package ecommerce.service.orders.mapper;

import java.util.ArrayList;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ecommerce.dto.orders.InOrderFilters;
import ecommerce.repository.orders.entity.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

@Component
public class OrdersSpecificationMapper {

    public Specification<Order> mapToSpecification(InOrderFilters filters) {
        return (root, query, cb) -> {
            final var predicates = new ArrayList<Predicate>();

            final var completed = filters.getCompleted();
            if (completed != null) {
                final Path<Boolean> path = root.get("completedAt");
                final Predicate predicate;
                if (Boolean.TRUE.equals(completed)) {
                    predicate = cb.isNotNull(path);
                } else {
                    predicate = cb.isNull(path);
                }
                predicates.add(predicate);
            }

            final var username = filters.getUsername();
            if (username != null) {
                final Path<String> path = root.get("username");
                final Predicate predicate = cb.equal(path, username);
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
