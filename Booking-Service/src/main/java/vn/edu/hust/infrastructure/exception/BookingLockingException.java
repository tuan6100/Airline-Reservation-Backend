package vn.edu.hust.infrastructure.exception;

public class BookingLockingException extends RuntimeException {
    public BookingLockingException(String message) {
        super(message);
    }

    public BookingLockingException(String message, Throwable cause) {
        super(message, cause);
    }
}
