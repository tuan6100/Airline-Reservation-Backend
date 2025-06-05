package vn.edu.hust.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketHoldExpiredEvent(
        Long ticketId,
        UUID ticketCode,
        Long customerId,
        Long seatId,
        String seatCode,
        Long flightId,
        LocalDateTime expiredAt,
        LocalDateTime originalHoldTime
) {}