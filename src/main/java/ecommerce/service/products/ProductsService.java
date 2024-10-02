package ecommerce.service.products;

import ecommerce.dto.products.InProduct;
import ecommerce.dto.products.InProductPatch;
import ecommerce.dto.products.InProductsFilters;
import ecommerce.dto.products.OutProduct;
import ecommerce.dto.shared.InPagination;
import ecommerce.dto.shared.OutPage;

public interface ProductsService {
    OutPage<OutProduct> getProducts(InProductsFilters filters, InPagination pagination);

    OutProduct postProduct(InProduct product);

    OutProduct getProduct(long id);

    void deleteProduct(long id);

    void patchProduct(long id, InProductPatch productPatch);
}
