package vn.edu.hust.integration.event;

public record PaymentFailedEvent(String paymentId, String orderId, String errorMessage) {}
