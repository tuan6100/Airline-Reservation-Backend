package vn.edu.hust.integration.event;

import vn.edu.hust.application.dto.query.TicketBookedDTO;

public record OrderCreationRequestEvent(
        String bookingId,
        Long customerId,
        TicketBookedDTO ticketBookedDTO
) {}