package vn.edu.hust.integration.event;

public record PaymentCompletedEvent(String paymentId, String orderId) {}
