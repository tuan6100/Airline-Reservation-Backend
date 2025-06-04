package vn.edu.hust.domain.event;

public record TicketAddedToBookingEvent(
        String bookingId,
        Long ticketId,
        Long seatId,
        Double price,
        String currency
) {}
