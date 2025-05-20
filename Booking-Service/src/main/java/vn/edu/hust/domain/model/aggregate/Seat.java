package vn.edu.hust.domain.model.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import vn.edu.hust.domain.event.SeatHeldEvent;
import vn.edu.hust.domain.event.SeatReleasedEvent;
import vn.edu.hust.domain.event.SeatReservedEvent;
import vn.edu.hust.domain.exception.SeatNotAvailableException;
import vn.edu.hust.domain.model.enumeration.SeatStatus;
import vn.edu.hust.domain.model.valueobj.*;
import vn.edu.hust.infrastructure.event.DomainEventPublisher;

import java.time.Duration;
import java.time.LocalDateTime;

@Aggregate
@NoArgsConstructor
@Getter
@Setter
public class Seat {
    @AggregateIdentifier
    private SeatId seatId;
    private FlightId flightId;
    private AircraftId aircraftId;
    private SeatClassId seatClassId;
    private SeatStatus status;
    private LocalDateTime holdUntil;
    private int version;

    // Constructor, getters, etc.

    // Factory method
    public static Seat create(SeatId seatId, FlightId flightId, AircraftId aircraftId,
                              SeatClassId seatClassId) {
        Seat seat = new Seat();
        seat.seatId = seatId;
        seat.flightId = flightId;
        seat.aircraftId = aircraftId;
        seat.seatClassId = seatClassId;
        seat.status = SeatStatus.AVAILABLE;
        seat.version = 0;

        return seat;
    }

    // Hold seat temporarily
    public void hold(CustomerId customerId, Duration holdDuration) {
        if (status != SeatStatus.AVAILABLE) {
            throw new SeatNotAvailableException("Seat is not available for holding");
        }

        status = SeatStatus.ON_HOLD;
        holdUntil = LocalDateTime.now().plus(holdDuration);
        version++;

        registerEvent(new SeatHeldEvent(seatId, flightId, customerId, holdUntil));
    }

    // Reserve seat permanently
    public void reserve(CustomerId customerId) {
        if (status == SeatStatus.RESERVED) {
            throw new SeatNotAvailableException("Seat is already reserved");
        }

        if (status == SeatStatus.ON_HOLD && LocalDateTime.now().isAfter(holdUntil)) {
            // Hold has expired
            release();
        }

        if (status != SeatStatus.AVAILABLE && status != SeatStatus.ON_HOLD) {
            throw new SeatNotAvailableException("Seat is not available for reservation");
        }

        status = SeatStatus.RESERVED;
        holdUntil = null;
        version++;

        registerEvent(new SeatReservedEvent(seatId, flightId, customerId));
    }

    // Release a seat
    public void release() {
        if (status == SeatStatus.AVAILABLE) {
            return;
        }

        status = SeatStatus.AVAILABLE;
        holdUntil = null;
        version++;

        registerEvent(new SeatReleasedEvent(seatId, flightId));
    }

    // Check if hold has expired
    public boolean isHoldExpired() {
        return status == SeatStatus.ON_HOLD && LocalDateTime.now().isAfter(holdUntil);
    }

    private void registerEvent(Object event) {
        DomainEventPublisher.instance().publish(event);
    }
}
