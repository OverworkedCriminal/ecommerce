package ecommerce.service.paymentmethods;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ecommerce.dto.paymentmethods.InPaymentMethod;
import ecommerce.dto.paymentmethods.InPaymentMethodPatch;
import ecommerce.dto.paymentmethods.OutPaymentMethod;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.repository.paymentmethods.PaymentMethodsRepository;
import ecommerce.repository.paymentmethods.entity.PaymentMethod;
import ecommerce.service.paymentmethods.mapper.PaymentMethodsMapper;
import ecommerce.service.utils.sanitizer.IUserInputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentMethodsService {

    private final PaymentMethodsMapper paymentMethodsMapper;
    private final IUserInputSanitizer userInputSanitizer;
    private final PaymentMethodsRepository paymentMethodsRepository;

    public PaymentMethod findByIdActive(long id) throws NotFoundException {
        final var payment = paymentMethodsRepository
            .findByIdAndActiveTrue(id)
            .orElseThrow(() -> NotFoundException.paymentMethod(id));
        return payment;
    }

    public List<OutPaymentMethod> getPaymentMethods() {
        final var paymentMethodEntities = paymentMethodsRepository.findByActiveTrue();
        log.info("found payment methods count={}", paymentMethodEntities.size());

        final var paymentMethods = paymentMethodEntities.stream()
            .map(paymentMethodsMapper::fromEntity)
            .collect(Collectors.toList());

        return paymentMethods;
    }

    public OutPaymentMethod getPaymentMethod(long id) throws NotFoundException {
        log.trace("id={}", id);

        final var entity = findByIdActive(id);
        log.info("found payment method with id={}", id);

        final var paymentMethod = paymentMethodsMapper.fromEntity(entity);
        return paymentMethod;
    }

    public OutPaymentMethod postPaymentMethod(
        InPaymentMethod inPaymentMethod
    ) throws ValidationException {
        log.trace("{}", inPaymentMethod);

        var entity = paymentMethodsMapper.intoEntity(inPaymentMethod);
        entity = paymentMethodsRepository.save(entity);
        log.info("created payment method with id={}", entity.getId());

        final var outPaymentMethod = paymentMethodsMapper.fromEntity(entity);
        return outPaymentMethod;
    }

    public void patchPaymentMethod(
        long id,
        InPaymentMethodPatch patch
    ) throws NotFoundException, ValidationException {
        log.trace("id={}", id);
        log.trace("{}", patch);

        final var entity = findByIdActive(id);
        log.info("found payment method with id={}", id);

        if (patch.name() != null) {
            final var name = userInputSanitizer.sanitize(patch.name());
            entity.setName(name);
        }
        if (patch.description() != null) {
            final var description = userInputSanitizer.sanitize(patch.description());
            entity.setDescription(description);
        }

        paymentMethodsRepository.save(entity);
        log.info("updated payment method with id={}", id);
    }

    public void deletePaymentMethod(long id) throws NotFoundException {
        log.trace("id={}", id);

        final var entity = findByIdActive(id);
        log.info("found payment method with id={}", id);

        entity.setActive(false);
        paymentMethodsRepository.save(entity);
        log.info("deleted payment method with id={}", id);
    }

}
