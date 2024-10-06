package ecommerce.repository.categories;

import org.springframework.data.jpa.repository.JpaRepository;

import ecommerce.repository.categories.entity.Category;

public interface CategoriesRepository extends JpaRepository<Category, Long> {

}
