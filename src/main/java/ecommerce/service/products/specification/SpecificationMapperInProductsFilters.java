package ecommerce.service.products.specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ecommerce.dto.products.InProductsFilters;
import ecommerce.repository.categories.CategoriesRepository;
import ecommerce.repository.categories.entity.Category;
import ecommerce.repository.products.entity.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SpecificationMapperInProductsFilters {

    private final CategoriesRepository categoriesRepository;

    public Specification<Product> mapToSpecification(InProductsFilters filters) {
        return (root, query, cb) -> {
            final var predicates = new ArrayList<Predicate>();

            final var name = filters.name();
            if (name != null) {
                final Path<String> path = root.get("name");
                final Predicate predicate = cb.like(
                    cb.upper(path),
                    name.toUpperCase()
                );
                predicates.add(predicate);
            }

            final var minPrice = filters.minPrice();
            if (minPrice != null) {
                final Path<BigDecimal> path = root.get("price");
                final Predicate predicate = cb.greaterThanOrEqualTo(path, minPrice);
                predicates.add(predicate);
            }

            final var maxPrice = filters.maxPrice();
            if (maxPrice != null) {
                final Path<BigDecimal> path = root.get("price");
                final Predicate predicate = cb.lessThanOrEqualTo(path, maxPrice);
                predicates.add(predicate);
            }

            final var category = filters.category();
            if (category != null) {
                final var categoryIds = findCategoriesTree(category);
                if (!categoryIds.isEmpty()) {
                    final Join<Product, Category> join = root.join("category", JoinType.INNER);
                    final Predicate predicate = join.get("id").in(categoryIds);
                    predicates.add(predicate);
                }
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            } else {
                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

    /**
     * Finds all child categories to categoryId and returns their ids.
     * (categoryId is included in result Set)
     * 
     * @param categoryId
     * @return
     */
    private Set<Long> findCategoriesTree(Long categoryId) {
        // TODO: This function requires optimization
        // Finding all categories can be achieved by single recursive query

        final var categoryIds = new HashSet<Long>();

        categoriesRepository
            .findById(categoryId)
            .ifPresent(category -> fillCategoryIds(categoryIds, category));

        return categoryIds;
    }

    private static void fillCategoryIds(Set<Long> categoryIds, Category category) {
        final var isUnique = categoryIds.add(category.getId());
        if (!isUnique) {
            log.error("detected cycle in categories relation id={}", category.getId());
            return;
        }

        category
            .getChildCategories()
            .forEach(childCategory -> fillCategoryIds(categoryIds, childCategory));
    }
}
