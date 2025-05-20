package vn.edu.hust.integration.event;

public record OrderConfirmedEvent(String orderId, String bookingId) {}
