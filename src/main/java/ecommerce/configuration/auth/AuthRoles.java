package ecommerce.configuration.auth;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthRoles {
    public static final String PRODUCT_CREATE = "ecommerce_create_product";
    public static final String PRODUCT_UPDATE = "ecommerce_update_product";
    public static final String PRODUCT_DELETE = "ecommerce_delete_product";

    public static final String ORDER_UPDATE_COMPLETED_AT = "ecommerce_update_order_completed_at";

    public static final String CATEGORY_MANAGE = "ecommerce_manage_category";

    public static final String COUNTRY_MANAGE = "ecommerce_manage_country";
}
