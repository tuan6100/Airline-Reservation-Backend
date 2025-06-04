package vn.edu.hust.domain.event;

import java.time.LocalDateTime;

public record TicketReleasedEvent(
        Long ticketId,
        Long flightId,
        Long seatId,
        LocalDateTime releasedAt
) {}
