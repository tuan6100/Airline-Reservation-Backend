package vn.edu.hust.domain.event;

import vn.edu.hust.application.dto.query.TicketBookedDTO;

public record OrderItemAddedEvent(
        String bookingId,
        TicketBookedDTO item
) {}