package vn.edu.hust.domain.model.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.domain.model.enumeration.BookingStatus;
import vn.edu.hust.domain.model.enumeration.CancellationReason;
import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.CustomerId;
import vn.edu.hust.domain.model.valueobj.SeatId;
import vn.edu.hust.domain.model.valueobj.SeatReservation;
import vn.edu.hust.infrastructure.event.DomainEventPublisher;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Aggregate
@NoArgsConstructor
@Getter
@Setter
public class Booking {
    @AggregateIdentifier
    private BookingId bookingId;
    private CustomerId customerId;
    private Set<SeatReservation> seatReservations;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;


    public static Booking create(BookingId bookingId, CustomerId customerId, List<SeatReservation> seatReservations) {
        Booking booking = new Booking();
        booking.bookingId = bookingId;
        booking.customerId = customerId;
        booking.seatReservations = new HashSet<>(seatReservations);
        booking.status = BookingStatus.PENDING;
        booking.createdAt = LocalDateTime.now();
        booking.expiresAt = LocalDateTime.now().plusMinutes(15);

        // Raise domain event
        booking.registerEvent(new BookingCreatedEvent(bookingId, customerId, seatReservations, booking.expiresAt));

        return booking;
    }

    public void confirm() {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking must be in PENDING state to be confirmed");
        }
        status = BookingStatus.CONFIRMED;
        registerEvent(new BookingConfirmedEvent(bookingId));
    }

    public void cancel(CancellationReason reason) {
        if (status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }
        status = BookingStatus.CANCELLED;
        registerEvent(new BookingCancelledEvent(bookingId, reason));
    }

    public void expire() {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can expire");
        }

        status = BookingStatus.EXPIRED;
        registerEvent(new BookingExpiredEvent(bookingId));
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void addSeatReservation(SeatReservation reservation) {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Cannot modify non-pending booking");
        }

        seatReservations.add(reservation);
        registerEvent(new SeatAddedToBookingEvent(bookingId, reservation.seatId()));
    }

    // Remove seat reservation
    public void removeSeatReservation(SeatId seatId) {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Cannot modify non-pending booking");
        }

        boolean removed = seatReservations.removeIf(sr -> sr.seatId().equals(seatId));

        if (removed) {
            registerEvent(new SeatRemovedFromBookingEvent(bookingId, seatId));
        }
    }

    // Private helper for domain events
    private void registerEvent(Object event) {
        DomainEventPublisher.instance().publish(event);
    }
}