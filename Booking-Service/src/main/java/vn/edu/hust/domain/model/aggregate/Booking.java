package vn.edu.hust.domain.model.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Value;
import vn.edu.hust.application.dto.command.*;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.domain.model.enumeration.BookingStatus;
import vn.edu.hust.domain.model.enumeration.CancellationReason;
import vn.edu.hust.domain.model.valueobj.SeatReservation;
import vn.edu.hust.domain.model.valueobj.TicketReservation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Aggregate
@NoArgsConstructor
@Getter
@Setter
public class Booking {
    @AggregateIdentifier
    private String bookingId;
    private Long customerId;
    private Set<SeatReservation> seatReservations = new HashSet<>();
    private Set<TicketReservation> ticketReservations = new HashSet<>();
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long flightId;
    private LocalDateTime flightDepartureTime;
    private Double totalAmount;
    private String currency;

    @Value("${domain.ticket.hold-util}")
    private CharSequence ticketHoldUtils;

    @CommandHandler
    public Booking(CreateBookingCommand command) {
        String bookingId = command.getBookingId() != null ?
                command.getBookingId() : UUID.randomUUID().toString();
        Set<SeatReservation> seatReservations = command.getTicketSelections().stream()
                .map(selection -> new SeatReservation(
                        selection.getSeatId(),
                        command.getFlightId(),
                        selection.getSeatClassId(),
                        selection.getPrice(),
                        selection.getCurrency()
                ))
                .collect(Collectors.toSet());
        double totalAmount = command.getTicketSelections().stream()
                .mapToDouble(CreateBookingCommand.TicketSelectionRequest::getPrice)
                .sum();
        LocalDateTime expiresAt = LocalDateTime.now().plus(
                Duration.parse(ticketHoldUtils)
        );
        AggregateLifecycle.apply(new BookingCreatedEvent(
                bookingId,
                command.getCustomerId(),
                seatReservations,
                expiresAt,
                command.getFlightId(),
                command.getFlightDepartureTime(),
                totalAmount,
                command.getCurrency() != null ? command.getCurrency() : "VND"
        ));
        command.getTicketSelections().forEach(selection -> {
            AggregateLifecycle.apply(new TicketAddedToBookingEvent(
                    bookingId,
                    selection.getTicketId(),
                    selection.getSeatId(),
                    selection.getPrice(),
                    selection.getCurrency()
            ));
        });
    }

    @CommandHandler
    public void handle(AddTicketToBookingCommand command) {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Can only add tickets to pending bookings");
        }
        if (isExpired()) {
            throw new IllegalStateException("Booking has expired");
        }
        boolean ticketExists = ticketReservations.stream()
                .anyMatch(tr -> tr.getTicketId().equals(command.getTicketId()));
        if (ticketExists) {
            throw new IllegalStateException("Ticket already added to booking");
        }
        AggregateLifecycle.apply(new TicketAddedToBookingEvent(
                bookingId,
                command.getTicketId(),
                command.getSeatId(),
                command.getPrice(),
                command.getCurrency()
        ));
    }

    @CommandHandler
    public void handle(RemoveTicketFromBookingCommand command) {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Can only remove tickets from pending bookings");
        }
        boolean ticketExists = ticketReservations.stream()
                .anyMatch(tr -> tr.getTicketId().equals(command.getTicketId()));
        if (!ticketExists) {
            throw new IllegalStateException("Ticket not found in booking");
        }
        AggregateLifecycle.apply(new TicketRemovedFromBookingEvent(
                bookingId,
                command.getTicketId()
        ));
    }

    @CommandHandler
    public void handle(ConfirmBookingCommand command) {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking must be in PENDING state to be confirmed");
        }
        if (isExpired()) {
            throw new IllegalStateException("Booking has expired");
        }
        if (ticketReservations.isEmpty()) {
            throw new IllegalStateException("Cannot confirm booking without tickets");
        }
        AggregateLifecycle.apply(new BookingConfirmedEvent(bookingId));
    }

    @CommandHandler
    public void handle(CancelBookingCommand command) {
        if (status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }
        CancellationReason reason = CancellationReason.valueOf(command.getReason());
        AggregateLifecycle.apply(new BookingCancelledEvent(bookingId, reason));
    }

    @CommandHandler
    public void handle(ExpireBookingCommand command) {
        if (status != BookingStatus.PENDING) {
            return;
        }
        AggregateLifecycle.apply(new BookingExpiredEvent(bookingId));
    }

    @EventSourcingHandler
    public void on(BookingCreatedEvent event) {
        this.bookingId = event.bookingId();
        this.customerId = event.customerId();
        this.seatReservations = new HashSet<>(event.seatReservations());
        this.status = BookingStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = event.expiresAt();
        this.flightId = event.flightId();
        this.flightDepartureTime = event.flightDepartureTime();
        this.totalAmount = event.totalAmount();
        this.currency = event.currency();
    }

    @EventSourcingHandler
    public void on(TicketAddedToBookingEvent event) {
        TicketReservation ticketReservation = new TicketReservation(
                event.ticketId(),
                event.seatId(),
                event.price(),
                event.currency()
        );
        this.ticketReservations.add(ticketReservation);
        recalculateTotalAmount();
    }

    @EventSourcingHandler
    public void on(TicketRemovedFromBookingEvent event) {
        this.ticketReservations.removeIf(tr -> tr.getTicketId().equals(event.ticketId()));

        recalculateTotalAmount();
    }

    @EventSourcingHandler
    public void on(BookingConfirmedEvent event) {
        this.status = BookingStatus.CONFIRMED;
    }

    @EventSourcingHandler
    public void on(BookingCancelledEvent event) {
        this.status = BookingStatus.CANCELLED;
    }

    @EventSourcingHandler
    public void on(BookingExpiredEvent event) {
        this.status = BookingStatus.EXPIRED;
    }

    private boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    private void recalculateTotalAmount() {
        double seatTotal = seatReservations.stream()
                .mapToDouble(SeatReservation::amount)
                .sum();

        double ticketTotal = ticketReservations.stream()
                .mapToDouble(TicketReservation::getPrice)
                .sum();

        this.totalAmount = seatTotal + ticketTotal;
    }

}