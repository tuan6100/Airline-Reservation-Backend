package vn.edu.hust.domain.event;

public record ItemRemovedEvent(
        Long orderId,
        String bookingId,
        Long ticketId
) {
}
