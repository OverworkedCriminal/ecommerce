package ecommerce.service.products;

import ecommerce.dto.products.InProduct;
import ecommerce.dto.products.InProductPatch;
import ecommerce.dto.products.InProductsFilters;
import ecommerce.dto.products.OutProduct;
import ecommerce.dto.products.OutProductDetails;
import ecommerce.dto.shared.InPagination;
import ecommerce.dto.shared.OutPage;

public interface IProductsService {
    OutPage<OutProduct> getProducts(InProductsFilters filters, InPagination pagination);

    OutProductDetails postProduct(InProduct product);

    OutProductDetails getProduct(long id);

    void deleteProduct(long id);

    void patchProduct(long id, InProductPatch productPatch);
}
