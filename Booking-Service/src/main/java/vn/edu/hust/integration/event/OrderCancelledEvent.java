package vn.edu.hust.integration.event;

public record OrderCancelledEvent(String orderId, String bookingId, String reason) {}
