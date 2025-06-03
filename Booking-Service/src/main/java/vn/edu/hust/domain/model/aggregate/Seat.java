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
    private SeatClassId seatClassId;
    private AircraftId aircraftId;
    private String seatCode;
    private Boolean isAvailable;
    private LocalDateTime holdUntil;
    private int version;

    public static Seat create(SeatClassId seatClassId, AircraftId aircraftId, String seatCode) {
        Seat seat = new Seat();
        seat.seatClassId = seatClassId;
        seat.aircraftId = aircraftId;
        seat.seatCode = seatCode;
        seat.isAvailable = true;
        seat.version = 0;
        return seat;
    }

    public void hold(Duration holdDuration) {
        if (!isAvailable) {
            throw new IllegalStateException("Seat is not available for holding");
        }
        isAvailable = false;
        holdUntil = LocalDateTime.now().plus(holdDuration);
        version++;
    }

    public void release() {
        isAvailable = true;
        holdUntil = null;
        version++;
    }

    public boolean isHoldExpired() {
        return holdUntil != null && LocalDateTime.now().isAfter(holdUntil);
    }

    private void registerEvent(Object event) {
        DomainEventPublisher.instance().publish(event);
    }
}
