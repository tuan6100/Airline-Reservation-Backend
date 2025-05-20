package vn.edu.hust.domain.model.valueobj;


public record BookingId(Long value) {
    public BookingId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Booking ID must be positive");
        }
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }
}
