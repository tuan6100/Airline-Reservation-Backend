package vn.edu.hust.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import vn.edu.hust.application.dto.query.TicketBookedDTO;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.infrastructure.mapper.TicketMapper;
import vn.edu.hust.integration.event.OrderCreationRequestEvent;

@Slf4j
@Component
public class KafkaEventPublisher {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TicketMapper ticketMapper;

    @EventListener
    public void handleTicketBookedEvent(TicketBookedEvent event) {
        try {
            TicketBookedDTO ticketBookedDTO = ticketMapper.fromEventToDTO(event);
            sendOrderCreationRequest(event, ticketBookedDTO);
        } catch (Exception e) {
            log.error("Failed to process ticket booked event for booking: {}", event.bookingId(), e);
        }
    }

    @Retryable(
            retryFor = {Exception.class},
            backoff = @Backoff(delay = 500)
    )
    public void sendOrderCreationRequest(TicketBookedEvent event, TicketBookedDTO ticketBookedDTO) throws Exception {
        OrderCreationRequestEvent orderEvent = new OrderCreationRequestEvent(
                event.bookingId(),
                event.customerId(),
                ticketBookedDTO
        );
        kafkaTemplate.send("order-creation-requests.topic", orderEvent).get();
        log.info("Successfully sent order creation request for booking: {}", event.bookingId());
    }

    @Deprecated
    @EventListener
    public void handleBookingCreatedEvent(BookingCreatedEvent event) {
        kafkaTemplate.send("booking-events.created", event);
    }

    @Deprecated
    @EventListener
    public void handleBookingConfirmedEvent(BookingConfirmedEvent event) {
        kafkaTemplate.send("booking-events.confirmed", event);
    }

    @EventListener
    public void handleBookingCancelledEvent(BookingCancelledEvent event) {
        kafkaTemplate.send("booking-events.cancelled", event);
    }


}