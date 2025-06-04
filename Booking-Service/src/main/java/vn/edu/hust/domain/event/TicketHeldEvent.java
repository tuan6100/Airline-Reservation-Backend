package vn.edu.hust.domain.event;

import java.time.LocalDateTime;

public record TicketHeldEvent(
        Long ticketId,
        Long flightId,
        Long seatId,
        Long customerId,
        LocalDateTime holdUntil
) {}