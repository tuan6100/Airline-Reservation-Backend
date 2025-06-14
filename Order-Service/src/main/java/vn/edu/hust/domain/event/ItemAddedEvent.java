package vn.edu.hust.domain.event;

import vn.edu.hust.application.dto.query.TicketBookedDTO;

public record ItemAddedEvent(
        String bookingId,
        TicketBookedDTO item
) {}