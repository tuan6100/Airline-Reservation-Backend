package vn.edu.hust.domain.model.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
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
import vn.edu.hust.application.dto.command.*;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.domain.model.enumeration.TicketStatus;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Aggregate
@NoArgsConstructor
@Getter
public class Ticket {
    @AggregateIdentifier
    private Long ticketId;
    private UUID ticketCode;
    private Long flightId;
    private LocalDateTime flightDepartureTime;
    private Long seatId;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime holdUntil;
    private Long heldByCustomerId;
    private String bookingId;
    private String seatCode;

    @Value("${domain.ticket.hold-util}")
    private CharSequence ticketHoldUtils;

    @Autowired
    private DeadlineManager deadlineManager;

    private Map<String, String> deadlineMap = new HashMap<>();

    @CommandHandler
    public Ticket(CreateTicketCommand command) {
        UUID ticketCode = command.getTicketCode() != null ?
                command.getTicketCode() : UUID.randomUUID();
        AggregateLifecycle.apply(new TicketCreatedEvent(
                command.getTicketId(),
                ticketCode,
                command.getFlightId(),
                command.getFlightDepartureTime(),
                command.getSeatId(),
                TicketStatus.AVAILABLE,
                LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(HoldTicketCommand command) {
        if (status != TicketStatus.AVAILABLE) {
            if (status == TicketStatus.HELD && isHoldExpired()) {
                handleExpiredHold();
            } else {
                throw new IllegalStateException("Ticket is not available for holding");
            }
        }
        LocalDateTime holdUntil = LocalDateTime.now().plus(
                Duration.parse(ticketHoldUtils)
        );
        AggregateLifecycle.apply(new TicketHeldEvent(
                ticketId,
                flightId,
                seatId,
                command.getCustomerId(),
                holdUntil
        ));
        String deadlineName = "ticket-hold-expired-" + ticketId + "-" + System.currentTimeMillis();
        String scheduledId = deadlineManager.schedule(
                Instant.from(holdUntil),
                deadlineName,
                new TicketHoldExpiredMessage(ticketId, command.getCustomerId())
        );
        deadlineMap.put(scheduledId, deadlineName);
    }

    @CommandHandler
    public void handle(BookTicketCommand command) {
        if (status != TicketStatus.HELD && status != TicketStatus.AVAILABLE) {
            throw new IllegalStateException("Ticket must be available or held to book");
        }
        if (status == TicketStatus.HELD && !command.getCustomerId().equals(heldByCustomerId)) {
            throw new IllegalStateException("Ticket is held by another customer");
        }
        if (!deadlineMap.isEmpty()) {
            Map.Entry<String, String> entry = deadlineMap.entrySet().iterator().next();
            deadlineManager.cancelSchedule(entry.getValue(), entry.getKey());
            deadlineMap.clear();
        }
        AggregateLifecycle.apply(new TicketBookedEvent(
                ticketId,
                flightId,
                seatId,
                command.getCustomerId(),
                command.getBookingId(),
                LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(ReleaseTicketCommand command) {
        if (status == TicketStatus.AVAILABLE) {
            return;
        }
        if (status == TicketStatus.BOOKED) {
            throw new IllegalStateException("Cannot release a booked ticket");
        }
        if (!deadlineMap.isEmpty()) {
            Map.Entry<String, String> entry = deadlineMap.entrySet().iterator().next();
            deadlineManager.cancelSchedule(entry.getValue(), entry.getKey());
            deadlineMap.clear();
        }
        AggregateLifecycle.apply(new TicketReleasedEvent(
                ticketId, flightId, seatId, LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(CancelTicketCommand command) {
        if (status == TicketStatus.CANCELLED) {
            throw new IllegalStateException("Ticket is already cancelled");
        }
        if (!deadlineMap.isEmpty()) {
            Map.Entry<String, String> entry = deadlineMap.entrySet().iterator().next();
            deadlineManager.cancelSchedule(entry.getValue(), entry.getKey());
            deadlineMap.clear();
        }
        AggregateLifecycle.apply(new TicketCancelledEvent(
                ticketId,
                flightId,
                seatId,
                command.getReason(),
                LocalDateTime.now()
        ));
    }

    @DeadlineHandler
    public void handle(TicketHoldExpiredMessage message) {
        if (status == TicketStatus.HELD && isHoldExpired()) {
            handleExpiredHold();
        }
    }

    private void handleExpiredHold() {
        LocalDateTime now = LocalDateTime.now();
        AggregateLifecycle.apply(new TicketHoldExpiredEvent(
                ticketId,
                ticketCode,
                heldByCustomerId,
                seatId,
                seatCode,
                flightId,
                now,
                holdUntil
        ));
        AggregateLifecycle.apply(new TicketReleasedEvent(
                ticketId, flightId, seatId, now
        ));
        deadlineMap.clear();
    }

    @EventSourcingHandler
    public void on(TicketCreatedEvent event) {
        this.ticketId = event.ticketId();
        this.ticketCode = event.ticketCode();
        this.flightId = event.flightId();
        this.flightDepartureTime = event.flightDepartureTime();
        this.seatId = event.seatId();
        this.status = event.status();
        this.createdAt = event.createdAt();
    }

    @EventSourcingHandler
    public void on(TicketHeldEvent event) {
        this.status = TicketStatus.HELD;
        this.holdUntil = event.holdUntil();
        this.heldByCustomerId = event.customerId();
    }

    @EventSourcingHandler
    public void on(TicketBookedEvent event) {
        this.status = TicketStatus.BOOKED;
        this.heldByCustomerId = event.customerId();
        this.bookingId = event.bookingId();
        this.holdUntil = null;
    }

    @EventSourcingHandler
    public void on(TicketReleasedEvent event) {
        this.status = TicketStatus.AVAILABLE;
        this.holdUntil = null;
        this.heldByCustomerId = null;
        this.bookingId = null;
    }

    @EventSourcingHandler
    public void on(TicketCancelledEvent event) {
        this.status = TicketStatus.CANCELLED;
    }

    @EventSourcingHandler
    public void on(TicketHoldExpiredEvent event) {
        Ticket.log.info("Ticket hold expired notification sent for ticket: {}", event.ticketId());
    }

    private boolean isHoldExpired() {
        return holdUntil != null && LocalDateTime.now().isAfter(holdUntil);
    }

    public record TicketHoldExpiredMessage(
            Long ticketId,
            Long customerId
    ) {}
}