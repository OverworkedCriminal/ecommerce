package ecommerce.repository.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ecommerce.repository.products.entity.Product;

public interface ProductsRepository 
extends
    JpaRepository<Product, Long>,
    JpaSpecificationExecutor<Product>
{
    
}
