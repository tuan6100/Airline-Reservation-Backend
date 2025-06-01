package vn.edu.hust.domain.exception;

public class OrderAlreadyConfirmedException extends RuntimeException {
    public OrderAlreadyConfirmedException(String message) {
        super(message);
    }

    public OrderAlreadyConfirmedException(String message, Throwable cause) {
        super(message, cause);
    }
}