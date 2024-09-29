package ecommerce.service.products;

import ecommerce.dto.products.InProduct;
import ecommerce.dto.products.OutProduct;
import ecommerce.dto.shared.InPagination;
import ecommerce.dto.shared.OutPage;

public interface ProductsService {
    OutPage<OutProduct> getProducts(InPagination pagination);

    OutProduct postProduct(InProduct product);
}
