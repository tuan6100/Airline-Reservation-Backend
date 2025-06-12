package vn.edu.hust.intergration.event;

import vn.edu.hust.application.dto.query.TicketBookedDTO;

public record OrderCreationRequestEvent(
        String bookingId,
        TicketBookedDTO ticketBookedDTO
) {}