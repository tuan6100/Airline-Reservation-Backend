package vn.edu.hust.domain.model.valueobj;

public record BookingId(String value) {  // Changed from Long to String
    public BookingId {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be null or empty");
        }
    }

    @Override
    public String toString() {
        return value != null ? value : "null";
    }
}