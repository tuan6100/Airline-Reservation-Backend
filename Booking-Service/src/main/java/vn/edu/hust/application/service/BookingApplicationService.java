package vn.edu.hust.application.service;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.hust.application.dto.command.*;
import vn.edu.hust.application.dto.query.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class BookingApplicationService {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private QueryGateway queryGateway;

    public CompletableFuture<String> createBooking(CreateBookingCommand command) {
        if (command.getBookingId() == null) {
            command.setBookingId(UUID.randomUUID().toString());
        }
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> addTicketToBooking(String bookingId, Long ticketId, Long seatId, Double price, String currency) {
        AddTicketToBookingCommand command = new AddTicketToBookingCommand();
        command.setBookingId(bookingId);
        command.setTicketId(ticketId);
        command.setSeatId(seatId);
        command.setPrice(price);
        command.setCurrency(currency);
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> removeTicketFromBooking(String bookingId, Long ticketId) {
        RemoveTicketFromBookingCommand command = new RemoveTicketFromBookingCommand();
        command.setBookingId(bookingId);
        command.setTicketId(ticketId);
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> expireBooking(String bookingId) {
        ExpireBookingCommand command = new ExpireBookingCommand();
        command.setBookingId(bookingId);
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> confirmBooking(String bookingId) {
        ConfirmBookingCommand command = new ConfirmBookingCommand();
        command.setBookingId(bookingId);
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> cancelBooking(String bookingId, String reason) {
        CancelBookingCommand command = new CancelBookingCommand();
        command.setBookingId(bookingId);
        command.setReason(reason);
        return commandGateway.send(command);
    }

    public CompletableFuture<BookingDTO> getBooking(String bookingId) {
        GetBookingQuery query = new GetBookingQuery();
        query.setBookingId(bookingId);
        return queryGateway.query(query, BookingDTO.class);
    }

    public CompletableFuture<List<BookingDTO>> getBookingsByCustomer(Long customerId) {
        GetBookingsByCustomerQuery query = new GetBookingsByCustomerQuery();
        query.setCustomerId(customerId);
        return queryGateway.query(query, ResponseTypes.multipleInstancesOf(BookingDTO.class));
    }

    public CompletableFuture<List<SeatDTO>> getAvailableSeats(Long flightId) {
        GetAvailableSeatsQuery query = new GetAvailableSeatsQuery();
        query.setFlightId(flightId);
        return queryGateway.query(query, ResponseTypes.multipleInstancesOf(SeatDTO.class));
    }
}