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

    /**
     * Find payment method by id
     * 
     * @param id
     * @return found payment method
     * @throws NotFoundException payment method does not exist or is inactive
     */
    public PaymentMethod findByIdActive(long id) throws NotFoundException {
        final var payment = paymentMethodsRepository
            .findByIdAndActiveTrue(id)
            .orElseThrow(() -> NotFoundException.paymentMethod(id));
        return payment;
    }

    /**
     * Find all active payment methods
     * 
     * @return found payment methods
     */
    public List<OutPaymentMethod> getPaymentMethods() {
        final var paymentMethodEntities = paymentMethodsRepository.findByActiveTrue();
        log.info("found payment methods count={}", paymentMethodEntities.size());

        final var paymentMethods = paymentMethodEntities.stream()
            .map(paymentMethodsMapper::fromEntity)
            .collect(Collectors.toList());

        return paymentMethods;
    }

    /**
     * Find payment method by id
     * 
     * @param id
     * @return found payment method
     * @throws NotFoundException payment method does not exist or is inactive
     */
    public OutPaymentMethod getPaymentMethod(long id) throws NotFoundException {
        log.trace("id={}", id);

        final var entity = findByIdActive(id);
        log.info("found payment method with id={}", id);

        final var paymentMethod = paymentMethodsMapper.fromEntity(entity);
        return paymentMethod;
    }

    /**
     * Create payment method
     * 
     * @param inPaymentMethod
     * @return created payment method
     * @throws ValidationException when 'name' or 'description' is invalid
     */
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

    /**
     * Update part of payment method
     * 
     * @param id
     * @param patch
     * @throws NotFoundException paymentMethod does not exist or is not active
     * @throws ValidationException 'name' or 'description' is invalid
     */
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

    /**
     * Delete payment method by changing 'active' to false
     * 
     * @param id
     * @throws NotFoundException payment method does not exist or is inactive
     */
    public void deletePaymentMethod(long id) throws NotFoundException {
        log.trace("id={}", id);

        final var entity = findByIdActive(id);
        log.info("found payment method with id={}", id);

        entity.setActive(false);
        paymentMethodsRepository.save(entity);
        log.info("deleted payment method with id={}", id);
    }

}
