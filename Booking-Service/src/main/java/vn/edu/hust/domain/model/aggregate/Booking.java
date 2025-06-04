package vn.edu.hust.domain.model.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import vn.edu.hust.application.dto.command.CancelBookingCommand;
import vn.edu.hust.application.dto.command.ConfirmBookingCommand;
import vn.edu.hust.application.dto.command.CreateBookingCommand;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.domain.model.enumeration.BookingStatus;
import vn.edu.hust.domain.model.enumeration.CancellationReason;
import vn.edu.hust.domain.model.valueobj.SeatReservation;

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
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @CommandHandler
    public Booking(CreateBookingCommand command) {
        String bookingId = UUID.randomUUID().toString();
        Set<SeatReservation> reservations = command.getSeatSelections().stream()
                .map(selection -> new SeatReservation(
                        selection.getSeatId(),
                        null,
                        selection.getSeatClassId(),
                        selection.getAmount(),
                        selection.getCurrency()
                ))
                .collect(Collectors.toSet());
        AggregateLifecycle.apply(new BookingCreatedEvent(
                bookingId,
                command.getCustomerId(),
                reservations,
                LocalDateTime.now().plusMinutes(15)
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

    @EventSourcingHandler
    public void on(BookingCreatedEvent event) {
        this.bookingId = event.bookingId();
        this.customerId = event.customerId();
        this.seatReservations = new HashSet<>(event.seatReservations());
        this.status = BookingStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = event.expiresAt();
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
}
