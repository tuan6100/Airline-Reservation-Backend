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
    public void handleDomainEvent(Object event) {
        String topic = determineTopicForEvent(event);
        kafkaTemplate.send(topic, event);
    }

    private String determineTopicForEvent(Object event) {
        if (event instanceof BookingCreatedEvent) {
            return "booking-events.created";
        } else if (event instanceof BookingConfirmedEvent) {
            return "booking-events.confirmed";
        } else if (event instanceof BookingCancelledEvent) {
            return "booking-events.cancelled";
        } else if (event instanceof BookingExpiredEvent) {
            return "booking-events.expired";
        } else if (event instanceof SeatHeldEvent ||
                event instanceof SeatReservedEvent ||
                event instanceof SeatReleasedEvent) {
            return "seat-events";
        }

        return "booking-events";
    }
}
