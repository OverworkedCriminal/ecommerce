package ecommerce.repository.products;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ecommerce.repository.products.entity.Product;

public interface ProductsRepository 
extends
    JpaRepository<Product, Long>,
    JpaSpecificationExecutor<Product>
{
    Optional<Product> findByIdAndActiveTrue(long id);

    List<Product> findByActiveTrueAndIdIn(Collection<Long> productIds);
}
