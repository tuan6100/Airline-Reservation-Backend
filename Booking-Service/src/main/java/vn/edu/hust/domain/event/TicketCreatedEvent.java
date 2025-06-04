package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.enumeration.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketCreatedEvent(
        Long ticketId,
        UUID ticketCode,
        Long flightId,
        LocalDateTime flightDepartureTime,
        Long seatId,
        TicketStatus status,
        LocalDateTime createdAt
) {}
