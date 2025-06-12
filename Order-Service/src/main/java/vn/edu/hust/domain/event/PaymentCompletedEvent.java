package vn.edu.hust.domain.event;

public record PaymentCompletedEvent(
    Long paymentId,
    Long orderId,
    Long bookingId
) {
}
