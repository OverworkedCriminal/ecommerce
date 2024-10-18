package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

import ecommerce.dto.paymentmethods.InPaymentMethod;
import ecommerce.dto.paymentmethods.InPaymentMethodPatch;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.repository.paymentmethods.PaymentMethodsRepository;
import ecommerce.repository.paymentmethods.entity.PaymentMethod;
import ecommerce.service.paymentmethods.PaymentMethodsService;
import ecommerce.service.paymentmethods.mapper.PaymentMethodsMapper;
import ecommerce.service.utils.sanitizer.IUserInputSanitizer;

public class PaymentMethodsServiceTest {

    private IUserInputSanitizer userInputSanitizer;
    private PaymentMethodsMapper paymentMethodsMapper;
    private PaymentMethodsRepository paymentMethodsRepository;

    @BeforeEach
    public void setupDependencies() throws ValidationException {
        userInputSanitizer = Mockito.mock(IUserInputSanitizer.class);
        Mockito
            .when(userInputSanitizer.sanitize(Mockito.anyString()))
            .then(AdditionalAnswers.returnsFirstArg());
        paymentMethodsMapper = new PaymentMethodsMapper(userInputSanitizer);
        paymentMethodsRepository = Mockito.mock(PaymentMethodsRepository.class);
    }

    private PaymentMethodsService createService() {
        return new PaymentMethodsService(
            paymentMethodsMapper,
            userInputSanitizer,
            paymentMethodsRepository
        );
    }

    //#region findByIdActive

    @Test
    public void findByIdActive_notFound() {
        final Long id = 1L;

        Mockito
            .doReturn(Optional.empty())
            .when(paymentMethodsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.findByIdActive(id);
        });
    }

    @Test
    public void findByIdActive() throws NotFoundException {
        final Long id = 1L;
        final var paymentMethod = PaymentMethod.builder()
            .id(id)
            .active(true)
            .name("name")
            .description("description")
            .build();

        Mockito
            .doReturn(Optional.of(paymentMethod))
            .when(paymentMethodsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        final var out = service.findByIdActive(id);

        assertEquals(paymentMethod.getId(), out.getId());
        assertEquals(paymentMethod.getActive(), out.getActive());
        assertEquals(paymentMethod.getName(), out.getName());
        assertEquals(paymentMethod.getDescription(), out.getDescription());
    }

    //#endregion

    //#region getPaymentMethods

    @Test
    public void getPaymentMethods() {
        final var paymentMethods = List.of(
            PaymentMethod.builder()
                .id(1L)
                .active(true)
                .name("name 1")
                .description("description 1")
                .build(),
            PaymentMethod.builder()
                .id(2L)
                .active(true)
                .name("name 2")
                .description("description 2")
                .build()
        );

        Mockito
            .doReturn(paymentMethods)
            .when(paymentMethodsRepository)
            .findByActiveTrue();

        final var service = createService();

        final var outPaymentMethods = service.getPaymentMethods();

        assertEquals(paymentMethods.size(), outPaymentMethods.size());
        for (int i = 0; i < paymentMethods.size(); ++i) {
            final var paymentMethod = paymentMethods.get(i);
            final var outPaymentMethod = outPaymentMethods.get(i);
            assertEquals(paymentMethod.getId(), outPaymentMethod.id());
            assertEquals(paymentMethod.getName(), outPaymentMethod.name());
            assertEquals(paymentMethod.getDescription(), outPaymentMethod.description());
        }
    }
    
    //#endregion

    //#region getPaymentMethod

    @Test
    public void getPaymentMethod_notFound() {
        final Long id = 1L;

        Mockito
            .doReturn(Optional.empty())
            .when(paymentMethodsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.getPaymentMethod(id);
        });
    }

    @Test
    public void getPaymentMethod() throws NotFoundException {
        final Long id = 1L;
        final var paymentMethod = PaymentMethod.builder()
            .id(id)
            .active(true)
            .name("name")
            .description("description")
            .build();

        Mockito
            .doReturn(Optional.of(paymentMethod))
            .when(paymentMethodsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        final var out = service.getPaymentMethod(id);

        assertEquals(paymentMethod.getId(), out.id());
        assertEquals(paymentMethod.getName(), out.name());
        assertEquals(paymentMethod.getDescription(), out.description());
    }
        
    //#endregion

    //#region postPaymentMethod

    @Test
    public void postPaymentMethod() throws ValidationException {
        final var inPaymentMethod = new InPaymentMethod("name", "description");
        final var paymentMethod = PaymentMethod.builder()
            .id(1L)
            .active(true)
            .name(inPaymentMethod.name())
            .description(inPaymentMethod.description())
            .build();

        Mockito
            .doReturn(paymentMethod)
            .when(paymentMethodsRepository)
            .save(Mockito.any());

        final var service = createService();

        final var out = service.postPaymentMethod(inPaymentMethod);

        assertEquals(paymentMethod.getId(), out.id());
        assertEquals(paymentMethod.getName(), out.name());
        assertEquals(paymentMethod.getDescription(), out.description());
        Mockito
            .verify(paymentMethodsRepository, Mockito.times(1))
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(true, saved.getActive());
                    assertEquals(inPaymentMethod.name(), saved.getName());
                    assertEquals(inPaymentMethod.description(), saved.getDescription());
                })
            );
    }

    @Test
    public void postPaymentMethod_sanitizerValidationException() throws Exception {
        final var inPaymentMethod = new InPaymentMethod("name", "description");

        Mockito
            .doThrow(ValidationException.class)
            .when(userInputSanitizer)
            .sanitize(Mockito.anyString());

        final var service = createService();

        assertThrows(ValidationException.class, () -> {
            service.postPaymentMethod(inPaymentMethod);
        });
    }

    //#endregion

    //#region patchPaymentMethod

    @Test
    public void patchPaymentMethod_notFound() {
        final Long id = 1L;
        final var inPatch = new InPaymentMethodPatch("name", "description");

        Mockito
            .doReturn(Optional.empty())
            .when(paymentMethodsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.patchPaymentMethod(id, inPatch);
        });
    }

    @Test
    public void patchPaymentMethod_sanitizerValidationException() throws ValidationException {
        final Long id = 1L;
        final var paymentMethod = PaymentMethod.builder()
            .id(id)
            .active(true)
            .name("name")
            .description("description")
            .build();
        final var inPatch = new InPaymentMethodPatch("name", "description");

        Mockito
            .doReturn(Optional.of(paymentMethod))
            .when(paymentMethodsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));
        Mockito
            .doThrow(ValidationException.class)
            .when(userInputSanitizer)
            .sanitize(Mockito.anyString());

        final var service = createService();

        assertThrows(ValidationException.class, () -> {
            service.patchPaymentMethod(id, inPatch);
        });
    }

    @Test
    public void patchPaymentMethod_nameUnchangedDescriptionChanged() throws NotFoundException, ValidationException {
        final Long id = 1L;
        final String name = "name";
        final String description = "description";
        final var paymentMethod = PaymentMethod.builder()
            .id(id)
            .active(true)
            .name(name)
            .description(description)
            .build();
        final var inPatch = new InPaymentMethodPatch(null, "new description");

        Mockito
            .doReturn(Optional.of(paymentMethod))
            .when(paymentMethodsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        service.patchPaymentMethod(id, inPatch);

        Mockito
            .verify(paymentMethodsRepository, Mockito.times(1))
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(id, saved.getId());
                    assertEquals(true, saved.getActive());
                    assertEquals(name, saved.getName());
                    assertEquals(inPatch.description(), saved.getDescription());
                })
            );
    }

    @Test
    public void patchPaymentMethod_nameChangeddescriptionUnchanged() throws NotFoundException, ValidationException {
        final Long id = 1L;
        final String name = "name";
        final String description = "description";
        final var paymentMethod = PaymentMethod.builder()
            .id(id)
            .active(true)
            .name(name)
            .description(description)
            .build();
        final var inPatch = new InPaymentMethodPatch("new name", null);

        Mockito
            .doReturn(Optional.of(paymentMethod))
            .when(paymentMethodsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        service.patchPaymentMethod(id, inPatch);

        Mockito
            .verify(paymentMethodsRepository, Mockito.times(1))
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(id, saved.getId());
                    assertEquals(true, saved.getActive());
                    assertEquals(inPatch.name(), saved.getName());
                    assertEquals(description, saved.getDescription());
                })
            );
    }

    //#endregion

    //#region deletePaymentMethod

    @Test
    public void deletePaymentMethod_notFound() {
        final Long id = 1L;

        Mockito
            .doReturn(Optional.empty())
            .when(paymentMethodsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.deletePaymentMethod(id);
        });
    }

    @Test
    public void deletePaymentMethod() throws NotFoundException {
        final Long id = 1L;
        final var paymentMethod = PaymentMethod.builder()
            .id(id)
            .active(true)
            .name("name")
            .description("description")
            .build();

        Mockito
            .doReturn(Optional.of(paymentMethod))
            .when(paymentMethodsRepository)
            .findByIdAndActiveTrue(Mockito.eq(id));

        final var service = createService();

        service.deletePaymentMethod(id);

        Mockito
            .verify(paymentMethodsRepository, Mockito.times(1))
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(id, saved.getId());
                    assertEquals(false, saved.getActive());
                })
            );
    }
    
    //#endregion
}
