package vn.edu.hust.domain.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.deadline.DeadlineManager;
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
import vn.edu.hust.domain.model.enumeration.SeatStatus;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

    @Value("${domain.seat.hold-util}")
    private CharSequence seatHoldUtils;

    @Autowired
    private DeadlineManager deadlineManager;

    private HashMap<String, String> deadlineMap;


    @CommandHandler
    public void handle(HoldSeatCommand command) {
        if (status != SeatStatus.AVAILABLE) {
            if (status == SeatStatus.ON_HOLD && isHoldExpired()) {
                AggregateLifecycle.apply(new SeatReleasedEvent(seatId, null));
            } else {
                throw new IllegalStateException("Seat is not available for holding");
            }
        }
        LocalDateTime holdUntil = LocalDateTime.now().plus(
                Duration.parse(seatHoldUtils)
        );
        AggregateLifecycle.apply(new SeatHeldEvent(
                seatId,
                null,
                command.getCustomerId(),
                holdUntil
        ));
        String deadlineName = "seat-hold-" + seatId + "-" + System.currentTimeMillis();
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
        }
        AggregateLifecycle.apply(new SeatReleasedEvent(seatId, null));
    }

    @EventSourcingHandler
    public void on(SeatHeldEvent event) {
        this.status = SeatStatus.ON_HOLD;
        this.holdUntil = event.holdUntil();
        this.heldByCustomerId = event.customerId();
    }

    @EventSourcingHandler
    public void on(SeatReleasedEvent event) {
        this.status = SeatStatus.AVAILABLE;
        this.holdUntil = null;
        this.heldByCustomerId = null;
    }

    private boolean isHoldExpired() {
        return holdUntil != null && LocalDateTime.now().isAfter(holdUntil);
    }

    private record SeatHoldExpiredMessage (
            Long seatId,
            Long customerId
    ) {}
}