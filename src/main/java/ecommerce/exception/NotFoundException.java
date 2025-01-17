package ecommerce.exception;

public class NotFoundException extends Exception {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException product(long id) {
        final var message = "product with id=%d not found".formatted(id);
        return new NotFoundException(message);
    }

    public static NotFoundException order(long id) {
        final var message = "order with id=%d not found".formatted(id);
        return new NotFoundException(message);
    }

    public static NotFoundException order(long id, String username) {
        final var message = "order with id=%d does not exist or does not belong to user=%s"
            .formatted(id, username);
        return new NotFoundException(message);
    }

    public static NotFoundException category(long id) {
        final var message = "category with id=%d not found".formatted(id);
        return new NotFoundException(message);
    }

    public static NotFoundException country(long id) {
        final var message = "country with id=%d not found".formatted(id);
        return new NotFoundException(message);
    }

    public static NotFoundException paymentMethod(long id) {
        final var message = "payment method with id=%d not found".formatted(id);
        return new NotFoundException(message);
    }

}
