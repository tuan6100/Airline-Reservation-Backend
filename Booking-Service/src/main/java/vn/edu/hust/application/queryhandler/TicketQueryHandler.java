package vn.edu.hust.application.queryhandler;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.edu.hust.application.dto.query.*;
import vn.edu.hust.infrastructure.repository.TicketQueryRepository;

import java.util.List;

@Component
public class TicketQueryHandler {

    @Autowired
    private TicketQueryRepository ticketQueryRepository;

    @QueryHandler
    public TicketDTO handle(GetTicketQuery query) {
        return ticketQueryRepository.findByTicketId(query.getTicketId());
    }

    @QueryHandler
    public List<TicketDTO> handle(GetTicketsByFlightQuery query) {
        return ticketQueryRepository.findByFlightId(query.getFlightId());
    }

    @QueryHandler
    public TicketDTO handle(GetAvailableTicketsQuery query) {
        return ticketQueryRepository.findAvailableByFlightId(query.getFlightId(), query.getFlightDepartureTime(), query.getSeatId());
    }

}
