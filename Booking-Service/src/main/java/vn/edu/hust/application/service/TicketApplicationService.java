package vn.edu.hust.application.service;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.hust.application.dto.command.*;
import vn.edu.hust.application.dto.query.TicketDTO;
import vn.edu.hust.application.dto.query.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TicketApplicationService {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private QueryGateway queryGateway;

    public CompletableFuture<Long> createTicket(CreateTicketCommand command) {
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> holdTicket(Long ticketId, Long customerId, Integer holdDurationMinutes) {
        HoldTicketCommand command = new HoldTicketCommand();
        command.setTicketId(ticketId);
        command.setCustomerId(customerId);
        command.setHoldDurationMinutes(holdDurationMinutes);
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> bookTicket(Long ticketId, Long customerId, String bookingId) {
        BookTicketCommand command = new BookTicketCommand();
        command.setTicketId(ticketId);
        command.setCustomerId(customerId);
        command.setBookingId(bookingId);
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> releaseTicket(Long ticketId) {
        ReleaseTicketCommand command = new ReleaseTicketCommand();
        command.setTicketId(ticketId);
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> cancelTicket(Long ticketId, String reason) {
        CancelTicketCommand command = new CancelTicketCommand();
        command.setTicketId(ticketId);
        command.setReason(reason);
        return commandGateway.send(command);
    }

    public CompletableFuture<TicketDTO> getTicket(Long ticketId) {
        GetTicketQuery query = new GetTicketQuery();
        query.setTicketId(ticketId);
        return queryGateway.query(query, TicketDTO.class);
    }

    public CompletableFuture<List<TicketDTO>> getTicketsByFlight(Long flightId) {
        GetTicketsByFlightQuery query = new GetTicketsByFlightQuery();
        query.setFlightId(flightId);
        return queryGateway.query(query, ResponseTypes.multipleInstancesOf(TicketDTO.class));
    }

    public CompletableFuture<List<TicketDTO>> getAvailableTickets(Long flightId) {
        GetAvailableTicketsQuery query = new GetAvailableTicketsQuery();
        query.setFlightId(flightId);
        return queryGateway.query(query, ResponseTypes.multipleInstancesOf(TicketDTO.class));
    }

    public CompletableFuture<List<TicketSummaryDTO>> getTicketsByCustomer(Long customerId) {
        GetTicketsByCustomerQuery query = new GetTicketsByCustomerQuery();
        query.setCustomerId(customerId);
        return queryGateway.query(query, ResponseTypes.multipleInstancesOf(TicketSummaryDTO.class));
    }
}
