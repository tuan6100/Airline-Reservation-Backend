package vn.edu.hust.domain.model.enumeration;

public enum OrderStatus {
    PENDING,        // Order created but not confirmed
    CONFIRMED,      // Order confirmed after booking is confirmed
    PAYMENT_PENDING, // Waiting for payment
    PAID,           // Payment completed
    CANCELLED,      // Order cancelled
    REFUNDED        // Order cancelled and refunded
}
