package vn.edu.hust.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vn.edu.hust.domain.event.SeatHoldExpiredEvent;
import vn.edu.hust.domain.event.TicketHoldExpiredEvent;
import vn.edu.hust.integration.dto.NotificationRequest;

@Slf4j
@Component
public class NotificationEventPublisher {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @EventListener
    public void handleSeatHoldExpired(SeatHoldExpiredEvent event) {
        NotificationRequest notification = NotificationRequest.builder()
                .customerId(event.customerId())
                .type("SEAT_HOLD_EXPIRED")
                .title("Seat Hold Expired")
                .message(String.format(
                        "Your hold on seat %s for flight %d has expired at %s. The seat is now available for other customers.",
                        event.seatCode(),
                        event.flightId(),
                        event.expiredAt().toString()
                ))
                .priority("MEDIUM")
                .category("BOOKING")
                .metadata(java.util.Map.of(
                        "seatId", event.seatId().toString(),
                        "seatCode", event.seatCode(),
                        "flightId", event.flightId().toString(),
                        "expiredAt", event.expiredAt().toString(),
                        "originalHoldTime", event.originalHoldTime().toString()
                ))
                .build();

        kafkaTemplate.send("notification-events.seat-hold-expired", notification);
        NotificationEventPublisher.log.info("Sent seat hold expired notification for customer: {}", event.customerId());
    }

    @EventListener
    public void handleTicketHoldExpired(TicketHoldExpiredEvent event) {
        NotificationRequest notification = NotificationRequest.builder()
                .customerId(event.customerId())
                .type("TICKET_HOLD_EXPIRED")
                .title("Ticket Hold Expired")
                .message(String.format(
                        "Your hold on ticket %s (seat %s) for flight %d has expired at %s. The ticket is now available for other customers.",
                        event.ticketCode().toString(),
                        event.seatCode(),
                        event.flightId(),
                        event.expiredAt().toString()
                ))
                .priority("HIGH")
                .category("BOOKING")
                .metadata(java.util.Map.of(
                        "ticketId", event.ticketId().toString(),
                        "ticketCode", event.ticketCode().toString(),
                        "seatId", event.seatId().toString(),
                        "seatCode", event.seatCode(),
                        "flightId", event.flightId().toString(),
                        "expiredAt", event.expiredAt().toString(),
                        "originalHoldTime", event.originalHoldTime().toString()
                ))
                .build();

        kafkaTemplate.send("notification-events.ticket-hold-expired", notification);
        NotificationEventPublisher.log.info("Sent ticket hold expired notification for customer: {}", event.customerId());
    }
}