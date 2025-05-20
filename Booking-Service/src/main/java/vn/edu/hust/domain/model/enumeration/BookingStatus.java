package vn.edu.hust.domain.model.enumeration;

public enum BookingStatus {
    PENDING,   // Initial state when booking is created
    CONFIRMED, // Booking is confirmed after payment
    CANCELLED, // Booking was cancelled
    EXPIRED    // Booking expired (hold time passed)
}

