package vn.edu.hust.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vn.edu.hust.domain.event.*;

@Component
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @EventListener
    public void handleBookingCreatedEvent(BookingCreatedEvent event) {
        kafkaTemplate.send("booking-events.created", event);
    }

    @EventListener
    public void handleBookingConfirmedEvent(BookingConfirmedEvent event) {
        kafkaTemplate.send("booking-events.confirmed", event);
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
        kafkaTemplate.send("seat-events", event);
    }

    @EventListener
    public void handleSeatReservedEvent(SeatReservedEvent event) {
        kafkaTemplate.send("seat-events", event);
    }

    @EventListener
    public void handleSeatReleasedEvent(SeatReleasedEvent event) {
        kafkaTemplate.send("seat-events", event);
    }

    @EventListener
    public void handleSeatAddedToBookingEvent(SeatAddedToBookingEvent event) {
        kafkaTemplate.send("seat-events", event);
    }

    @EventListener
    public void handleSeatRemovedFromBookingEvent(SeatRemovedFromBookingEvent event) {
        kafkaTemplate.send("seat-events", event);
    }
}