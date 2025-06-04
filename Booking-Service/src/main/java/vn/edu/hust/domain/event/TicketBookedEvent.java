package vn.edu.hust.domain.event;

import java.time.LocalDateTime;

public record TicketBookedEvent(
        Long ticketId,
        Long flightId,
        Long seatId,
        Long customerId,
        String bookingId,
        LocalDateTime bookedAt
) {}
