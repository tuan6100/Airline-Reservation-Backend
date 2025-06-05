package vn.edu.hust.application.service;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.application.dto.command.*;
import vn.edu.hust.application.dto.query.*;
import vn.edu.hust.domain.model.enumeration.TicketStatus;
import vn.edu.hust.infrastructure.entity.TicketEntity;
import vn.edu.hust.infrastructure.repository.TicketJpaRepository;
import vn.edu.hust.infrastructure.repository.SeatClassJpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class BookingApplicationService {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private QueryGateway queryGateway;

    @Autowired
    private TicketSearchService ticketSearchService;

    @Autowired
    private TicketJpaRepository ticketRepository;

    @Autowired
    private SeatClassJpaRepository seatClassRepository;

    public CompletableFuture<TicketAvailabilityDTO> searchAvailableTickets(Long flightId) {
        try {
            TicketAvailabilityDTO availability = ticketSearchService.getFlightAvailability(flightId);
            return CompletableFuture.completedFuture(availability);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<List<TicketSearchDTO>> searchTicketsBySeatClass(Long flightId, Long seatClassId) {
        try {
            List<TicketSearchDTO> tickets = ticketSearchService.searchAvailableTickets(flightId, seatClassId);
            return CompletableFuture.completedFuture(tickets);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Transactional
    public CompletableFuture<String> createBookingWithTickets(CreateBookingCommand command) {
        return validateAndHoldTickets(command)
                .thenCompose(validatedCommand -> {
                    if (validatedCommand.getBookingId() == null) {
                        validatedCommand.setBookingId(UUID.randomUUID().toString());
                    }
                    return commandGateway.send(validatedCommand);
                });
    }

    private CompletableFuture<CreateBookingCommand> validateAndHoldTickets(CreateBookingCommand command) {
        return CompletableFuture.supplyAsync(() -> {
            for (CreateBookingCommand.TicketSelectionRequest selection : command.getTicketSelections()) {
                TicketEntity ticket = ticketRepository.findById(selection.getTicketId())
                        .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + selection.getTicketId()));

                if (ticket.getStatus() != TicketStatus.AVAILABLE) {
                    throw new IllegalStateException("Ticket " + selection.getTicketId() + " is not available");
                }
                Double correctPrice = getPriceFromSeatClass(selection.getSeatClassId());
                selection.setPrice(correctPrice);
                selection.setCurrency("VND");
                holdTicketInternal(ticket);
            }

            return command;
        });
    }

    private Double getPriceFromSeatClass(Long seatClassId) {
        try {
            return seatClassRepository.findById(seatClassId.intValue())
                    .map(seatClass -> seatClass.getPrice().doubleValue())
                    .orElseThrow(() -> new IllegalArgumentException("SeatClass not found: " + seatClassId));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get price for seat class: " + seatClassId, e);
        }
    }

    private void holdTicketInternal(TicketEntity ticket) {
        ticket.setStatus(TicketStatus.HELD);
        ticketRepository.save(ticket);
    }


    @Transactional
    public CompletableFuture<Void> confirmBookingAndCreateOrder(String bookingId) {
        return getBooking(bookingId)
                .thenCompose(booking -> {
                    if (booking == null) {
                        throw new IllegalArgumentException("Booking not found: " + bookingId);
                    }
                    return confirmBooking(bookingId)
                            .thenCompose(result -> {
                                return triggerOrderCreation(booking);
                            });
                });
    }

    private CompletableFuture<Void> triggerOrderCreation(BookingDTO booking) {
        return CompletableFuture.runAsync(() -> {
            BookingApplicationService.log.info("Booking confirmed, Order Service will receive event: {}", booking.getBookingId());
        });
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

    public CompletableFuture<Void> confirmBooking(String bookingId) {
        ConfirmBookingCommand command = new ConfirmBookingCommand();
        command.setBookingId(bookingId);
        return commandGateway.send(command);
    }

    public CompletableFuture<BookingDTO> getBooking(String bookingId) {
        GetBookingQuery query = new GetBookingQuery();
        query.setBookingId(bookingId);
        return queryGateway.query(query, BookingDTO.class);
    }

    public void cancelBooking(String bookingId, String reason) {
        CancelBookingCommand command = new CancelBookingCommand();
        command.setBookingId(bookingId);
        command.setReason(reason);
        commandGateway.send(command);
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