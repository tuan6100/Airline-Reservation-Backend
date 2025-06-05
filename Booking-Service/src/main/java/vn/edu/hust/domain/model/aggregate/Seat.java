package vn.edu.hust.domain.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import vn.edu.hust.application.dto.command.HoldSeatCommand;
import vn.edu.hust.application.dto.command.ReleaseSeatCommand;
import vn.edu.hust.domain.event.SeatHeldEvent;
import vn.edu.hust.domain.event.SeatReleasedEvent;
import vn.edu.hust.domain.event.SeatHoldExpiredEvent;
import vn.edu.hust.domain.model.enumeration.SeatStatus;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aggregate
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Seat {
    @AggregateIdentifier
    private Long seatId;
    private Long seatClassId;
    private Long aircraftId;
    private String seatCode;
    private SeatStatus status;
    private LocalDateTime holdUntil;
    private Long heldByCustomerId;
    private Integer version;
    private Long flightId;

    @Value("${domain.seat.hold-util}")
    private CharSequence seatHoldUtils;

    @Autowired
    private DeadlineManager deadlineManager;

    private Map<String, String> deadlineMap = new HashMap<>();

    @CommandHandler
    public void handle(HoldSeatCommand command) {
        if (status != SeatStatus.AVAILABLE) {
            if (status == SeatStatus.ON_HOLD && isHoldExpired()) {
                handleExpiredHold();
            } else {
                throw new IllegalStateException("Seat is not available for holding");
            }
        }
        LocalDateTime holdUntil = LocalDateTime.now().plus(
                Duration.parse(seatHoldUtils)
        );
        AggregateLifecycle.apply(new SeatHeldEvent(
                seatId,
                command.getFlightId(),
                command.getCustomerId(),
                holdUntil
        ));
        String deadlineName = "seat-hold-expired-" + seatId + "-" + System.currentTimeMillis();
        String scheduledId = deadlineManager.schedule(
                Instant.from(holdUntil),
                deadlineName,
                new SeatHoldExpiredMessage(seatId, command.getCustomerId())
        );
        deadlineMap.put(scheduledId, deadlineName);
    }

    @CommandHandler
    public void handle(ReleaseSeatCommand command) {
        if (status == SeatStatus.AVAILABLE) {
            return;
        }
        if (!deadlineMap.isEmpty()) {
            Map.Entry<String, String> entry = deadlineMap.entrySet().iterator().next();
            deadlineManager.cancelSchedule(entry.getValue(), entry.getKey());
            deadlineMap.clear();
        }
        AggregateLifecycle.apply(new SeatReleasedEvent(seatId, flightId));
    }

    @DeadlineHandler
    public void handle(SeatHoldExpiredMessage message) {
        if (status == SeatStatus.ON_HOLD && isHoldExpired()) {
            handleExpiredHold();
        }
    }

    private void handleExpiredHold() {
        LocalDateTime now = LocalDateTime.now();
        AggregateLifecycle.apply(new SeatHoldExpiredEvent(
                seatId,
                heldByCustomerId,
                seatCode,
                flightId,
                now,
                holdUntil
        ));
        AggregateLifecycle.apply(new SeatReleasedEvent(seatId, flightId));
        deadlineMap.clear();
    }

    @EventSourcingHandler
    public void on(SeatHeldEvent event) {
        this.status = SeatStatus.ON_HOLD;
        this.holdUntil = event.holdUntil();
        this.heldByCustomerId = event.customerId();
        this.flightId = event.flightId();
    }

    @EventSourcingHandler
    public void on(SeatReleasedEvent event) {
        this.status = SeatStatus.AVAILABLE;
        this.holdUntil = null;
        this.heldByCustomerId = null;
    }

    @EventSourcingHandler
    public void on(SeatHoldExpiredEvent event) {
        Seat.log.info("Seat hold expired notification sent for seat: {}", event.seatId());
    }

    private boolean isHoldExpired() {
        return holdUntil != null && LocalDateTime.now().isAfter(holdUntil);
    }

    public record SeatHoldExpiredMessage(
            Long seatId,
            Long customerId
    ) {}
}