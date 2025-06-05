package vn.edu.hust.integration.event;

public record OrderCreationRequestEvent(
        String bookingId,
        String correlationId
) {}