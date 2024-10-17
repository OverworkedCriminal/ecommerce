package ecommerce.service.paymentmethods.mapper;

import org.springframework.stereotype.Component;

import ecommerce.dto.paymentmethods.InPaymentMethod;
import ecommerce.dto.paymentmethods.OutPaymentMethod;
import ecommerce.exception.ValidationException;
import ecommerce.repository.paymentmethods.entity.PaymentMethod;
import ecommerce.service.utils.sanitizer.IUserInputSanitizer;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentMethodsMapper {

    private final IUserInputSanitizer userInputSanitizer;

    public PaymentMethod intoEntity(InPaymentMethod paymentMethod) throws ValidationException {
        final String name = userInputSanitizer.sanitize(paymentMethod.name());
        final String description = userInputSanitizer.sanitize(paymentMethod.description());

        return PaymentMethod.builder()
            .active(true)
            .name(name)
            .description(description)
            .build();
    }

    public OutPaymentMethod fromEntity(PaymentMethod paymentMethod) {
        return OutPaymentMethod.builder()
            .id(paymentMethod.getId())
            .name(paymentMethod.getName())
            .description(paymentMethod.getDescription())
            .build();
    }
}
