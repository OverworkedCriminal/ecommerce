package ecommerce.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException product(long id) {
        final var message = "product with id=%d not found".formatted(id);
        return new NotFoundException(message);
    }

}
