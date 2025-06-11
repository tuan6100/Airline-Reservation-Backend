package vn.edu.hust.application.service;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.application.dto.command.*;
import vn.edu.hust.application.dto.query.*;
import vn.edu.hust.domain.event.BookingConfirmedEvent;
import vn.edu.hust.domain.model.enumeration.BookingStatus;
import vn.edu.hust.domain.model.enumeration.TicketStatus;
import vn.edu.hust.infrastructure.entity.BookingEntity;
import vn.edu.hust.infrastructure.event.KafkaEventPublisher;
import vn.edu.hust.infrastructure.repository.BookingJpaRepository;
import vn.edu.hust.infrastructure.repository.TicketJpaRepository;
import vn.edu.hust.infrastructure.repository.SeatClassJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    private BookingJpaRepository bookingRepository;

    @Autowired
    private TicketJpaRepository ticketRepository;

    @Autowired
    private SeatClassJpaRepository seatClassRepository;

    @Autowired
    private KafkaEventPublisher kafkaEventPublisher;

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

    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 30)
    public CompletableFuture<String> createBookingWithTickets(CreateBookingCommand command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (command.getBookingId() == null) {
                    command.setBookingId(UUID.randomUUID().toString());
                }
                if (!validateAndHoldTicketsOptimized(command)) {
                    throw new RuntimeException("Failed to hold all required tickets");
                }
                String bookingId = commandGateway.sendAndWait(command);
                log.info("Booking created successfully with optimized approach: {}", bookingId);
                return bookingId;
            } catch (Exception e) {
                log.error("Failed to create booking: {}", e.getMessage());
                throw new RuntimeException("Failed to create booking: " + e.getMessage(), e);
            }
        });
    }

    private boolean validateAndHoldTicketsOptimized(CreateBookingCommand command) {
        List<Long> ticketIds = command.getTicketSelections()
                .stream()
                .map(CreateBookingCommand.TicketSelectionRequest::getTicketId)
                .collect(Collectors.toList());
        List<Long> availableTickets = ticketRepository.findAvailableTicketIds(ticketIds);
        if (availableTickets.size() != ticketIds.size()) {
            log.warn("Not all tickets available. Required: {}, Available: {}",
                    ticketIds.size(), availableTickets.size());
            return false;
        }
        for (CreateBookingCommand.TicketSelectionRequest selection : command.getTicketSelections()) {
            if (!holdTicketAtomically(selection, command.getBookingId())) {
                rollbackHeldTickets(command.getBookingId());
                return false;
            }
        }
        return true;
    }

    private boolean holdTicketAtomically(CreateBookingCommand.TicketSelectionRequest selection, String bookingId) {
        try {
            Double correctPrice = getPriceFromSeatClass(selection.getSeatClassId());
            selection.setPrice(correctPrice);
            selection.setCurrency("VND");
            int rowsUpdated = ticketRepository.updateTicketStatusAtomic(
                    selection.getTicketId(),
                    TicketStatus.AVAILABLE,
                    TicketStatus.HELD,
                    bookingId,
                    LocalDateTime.now()
            );
            if (rowsUpdated > 0) {
                log.debug("Ticket {} held atomically for booking {}", selection.getTicketId(), bookingId);
                return true;
            } else {
                log.warn("Failed to hold ticket {} - may already be taken", selection.getTicketId());
                return false;
            }
        } catch (Exception e) {
            log.error("Error holding ticket {}: {}", selection.getTicketId(), e.getMessage());
            return false;
        }
    }

    private void rollbackHeldTickets(String bookingId) {
        try {
            int releasedCount = ticketRepository.bulkReleaseTicketsByBooking(bookingId, LocalDateTime.now());
            log.info("Rolled back {} tickets for booking {}", releasedCount, bookingId);
        } catch (Exception e) {
            log.error("Failed to rollback tickets for booking {}: {}", bookingId, e.getMessage());
        }
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

    @Deprecated
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CompletableFuture<Void> confirmBookingAndCreateOrder(String bookingId) {
        return CompletableFuture.runAsync(() -> {
            try {
                BookingEntity booking = bookingRepository.findByIdWithLock(bookingId);
                if (booking == null) {
                    throw new IllegalArgumentException("Booking not found: " + bookingId);
                }
                if (booking.getStatus() != BookingStatus.PENDING) {
                    throw new IllegalStateException("Booking is not in PENDING status: " + booking.getStatus());
                }
                if (booking.isExpired()) {
                    throw new IllegalStateException("Booking has expired");
                }
                ConfirmBookingCommand confirmCommand = new ConfirmBookingCommand();
                confirmCommand.setBookingId(bookingId);
                commandGateway.sendAndWait(confirmCommand);
                triggerOrderCreation(booking);
                log.info("Booking confirmed and order creation triggered: {}", bookingId);
            } catch (Exception e) {
                log.error("Failed to confirm booking and create order: {}", e.getMessage());
                throw new RuntimeException("Failed to confirm booking: " + e.getMessage(), e);
            }
        });
    }

    @Deprecated
    private void triggerOrderCreation(BookingEntity booking) {
        try {
            kafkaEventPublisher.handleBookingConfirmedEvent(new BookingConfirmedEvent(booking.getBookingId()));
        } catch (Exception e) {
            log.error("Failed to publish order creation event for booking {}: {}",
                    booking.getBookingId(), e.getMessage());
        }
    }

    @Transactional
    public CompletableFuture<Integer> releaseBookingTickets(String bookingId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return ticketRepository.bulkReleaseTicketsByBooking(bookingId, LocalDateTime.now());
            } catch (Exception e) {
                log.error("Failed to release tickets for booking {}: {}", bookingId, e.getMessage());
                return 0;
            }
        });
    }

    public void confirmBooking(String bookingId) {
        ConfirmBookingCommand command = new ConfirmBookingCommand();
        command.setBookingId(bookingId);
        commandGateway.send(command);
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
        releaseBookingTickets(bookingId);
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