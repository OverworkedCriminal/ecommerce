package ecommerce.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public static ConflictException orderAlreadyCompleted(long id) {
        final var message = "order with id=%d have already been completed".formatted(id);
        return new ConflictException(message);
    }
}
