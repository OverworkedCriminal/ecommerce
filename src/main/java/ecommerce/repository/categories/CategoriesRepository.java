package ecommerce.repository.categories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ecommerce.repository.categories.entity.Category;

public interface CategoriesRepository extends JpaRepository<Category, Long> {

    @Query(
        value = """
            WITH RECURSIVE category_ids (id, parent_category_id) AS (
                SELECT id, parent_category_id FROM categories WHERE id = :categoryId
                UNION ALL
                SELECT c.id, c.parent_category_id FROM categories c INNER JOIN category_ids ids ON c.parent_category_id = ids.id
            )
            SELECT id FROM category_ids
        """,
        nativeQuery = true
    )
    List<Long> findCategoryIdsTree(Long categoryId);

}
