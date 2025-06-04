package vn.edu.hust.domain.event;

public record TicketRemovedFromBookingEvent(
        String bookingId,
        Long ticketId
) {}
