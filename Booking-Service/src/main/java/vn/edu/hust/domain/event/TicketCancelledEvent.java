package vn.edu.hust.domain.event;

import java.time.LocalDateTime;

public record TicketCancelledEvent(
        Long ticketId,
        Long flightId,
        Long seatId,
        String reason,
        LocalDateTime cancelledAt
) {}
