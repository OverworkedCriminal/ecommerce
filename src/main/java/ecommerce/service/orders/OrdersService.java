package ecommerce.service.orders;

import org.springframework.security.core.Authentication;

import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.OutOrder;

public interface OrdersService {
    
    OutOrder postOrder(Authentication auth, InOrder order);
}
