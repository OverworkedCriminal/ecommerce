package ecommerce.configuration.auth;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthRoles {
    public static final String CREATE_PRODUCT = "ecommerce_create_product";
    public static final String UPDATE_PRODUCT = "ecommerce_update_product";
    public static final String DELETE_PRODUCT = "ecommerce_delete_product";

    public static final String UPDATE_ORDER_COMPLETED_AT = "ecommerce_update_order_completed_at";

    public static final String MANAGE_CATEGORY = "ecommerce_manage_category";
}
