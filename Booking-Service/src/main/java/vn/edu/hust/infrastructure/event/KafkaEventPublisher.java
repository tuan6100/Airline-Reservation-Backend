package vn.edu.hust.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.integration.event.OrderCreationRequestEvent;

@Component
public class KafkaEventPublisher {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @EventListener
    public void handleBookingCreatedEvent(BookingCreatedEvent event) {
        kafkaTemplate.send("booking-events.created", event);
    }

    @EventListener
    public void handleBookingConfirmedEvent(BookingConfirmedEvent event) {
        kafkaTemplate.send("booking-events.confirmed", event);
        OrderCreationRequestEvent orderEvent = new OrderCreationRequestEvent(
                event.bookingId(),
                "ORDER_FROM_BOOKING_" + System.currentTimeMillis()
        );
        kafkaTemplate.send("order-creation-requests", orderEvent);
    }

    @EventListener
    public void handleBookingCancelledEvent(BookingCancelledEvent event) {
        kafkaTemplate.send("booking-events.cancelled", event);
    }

    @EventListener
    public void handleBookingExpiredEvent(BookingExpiredEvent event) {
        kafkaTemplate.send("booking-events.expired", event);
    }

    @EventListener
    public void handleSeatHeldEvent(SeatHeldEvent event) {
        kafkaTemplate.send("seat-events.held", event);
    }

    @EventListener
    public void handleSeatReservedEvent(SeatReservedEvent event) {
        kafkaTemplate.send("seat-events.reserved", event);
    }

    @EventListener
    public void handleSeatReleasedEvent(SeatReleasedEvent event) {
        kafkaTemplate.send("seat-events.released", event);

    }

    @EventListener
    public void handleSeatAddedToBookingEvent(SeatAddedToBookingEvent event) {
        kafkaTemplate.send("seat-events.added-to-booking", event);
    }

    @EventListener
    public void handleSeatRemovedFromBookingEvent(SeatRemovedFromBookingEvent event) {
        kafkaTemplate.send("seat-events.removed-from-booking", event);
    }

    @EventListener
    public void handleSeatHoldExpiredEvent(SeatHoldExpiredEvent event) {
        kafkaTemplate.send("seat-events.hold-expired", event);
    }

    @EventListener
    public void handleTicketReleasedEvent(TicketReleasedEvent event) {
        kafkaTemplate.send("ticket-events.released", event);
    }

    @EventListener
    public void handleTicketHoldExpiredEvent(TicketHoldExpiredEvent event) {
        kafkaTemplate.send("ticket-events.hold-expired", event);
    }


}