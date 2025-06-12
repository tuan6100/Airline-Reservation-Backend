package vn.edu.hust.application.service;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.application.dto.command.*;
import vn.edu.hust.application.dto.query.TicketDTO;
import vn.edu.hust.application.dto.query.*;
import vn.edu.hust.infrastructure.entity.TicketEntity;
import vn.edu.hust.infrastructure.repository.TicketJpaRepository;
import vn.edu.hust.infrastructure.event.KafkaEventPublisher;
import vn.edu.hust.domain.event.TicketBookedEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TicketApplicationService {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private QueryGateway queryGateway;

    @Autowired
    private TicketJpaRepository ticketRepository;

    @Autowired
    private KafkaEventPublisher kafkaEventPublisher;

    public CompletableFuture<List<TicketDTO>> getTicketsBySeatAndFlight(
            List<SeatAndFlightDTO> seatAndFlightDTOList
    ) {
        return CompletableFuture.supplyAsync(() -> seatAndFlightDTOList.stream()
                .map(seatAndFlightDTO -> {
                    TicketEntity ticket = ticketRepository.findTicketsBySeatAndFlightAndTime(
                            seatAndFlightDTO.getSeatId(),
                            seatAndFlightDTO.getFlightId(),
                            seatAndFlightDTO.getFlightDepartureTime()
                    );
                    if (ticket == null) {
                        return null;
                    }
                    TicketDTO dto = new TicketDTO();
                    dto.setTicketId(ticket.getTicketId());
                    dto.setTicketCode(ticket.getTicketCode());
                    dto.setSeatId(ticket.getSeat().getSeatId());
                    dto.setStatus(ticket.getStatus());
                    dto.setCreatedAt(ticket.getCreatedAt());
                    dto.setBookingId(ticket.getBookingId());
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 30)
    public CompletableFuture<String> bookTicket(BookTicketCommand command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean canBook = ticketRepository.canBookTicketAtomic(
                        command.getTicketId(),
                        command.getCustomerId(),
                        command.getBookingId()
                );
                if (!canBook) {
                    throw new IllegalStateException("Ticket is not available for booking");
                }
                commandGateway.sendAndWait(command);
                TicketEntity ticketEntity = ticketRepository.findByIdWithLock(command.getTicketId());
                TicketBookedEvent event = new TicketBookedEvent(
                        command.getTicketId(),
                        ticketEntity.getFlightId(),
                        ticketEntity.getSeat().getSeatId(),
                        command.getCustomerId(),
                        command.getBookingId(),
                        LocalDateTime.now()
                );

                kafkaEventPublisher.handleTicketBookedEvent(event);
                return "Ticket booked successfully";
            } catch (Exception e) {
                throw new RuntimeException("Failed to book ticket: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Void> holdTicket(Long ticketId, Long customerId) {
        HoldTicketCommand command = new HoldTicketCommand();
        command.setTicketId(ticketId);
        command.setCustomerId(customerId);
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

    public CompletableFuture<TicketDTO> getAvailableTickets(
            Long flightId,
            LocalDateTime flightDepartureTime,
            Long seatId
    ) {
        GetAvailableTicketsQuery query = new GetAvailableTicketsQuery();
        query.setFlightId(flightId);
        query.setFlightDepartureTime(flightDepartureTime);
        query.setSeatId(seatId);
        return queryGateway.query(query, TicketDTO.class);
    }
}