package vn.edu.hust.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.domain.model.valueobj.OrderId;
import vn.edu.hust.infrastructure.outbox.OutboxMessage;
import vn.edu.hust.infrastructure.outbox.OutboxService;

import java.time.LocalDateTime;

/**
 * Event publisher that uses Kafka
 */
@Component
public class KafkaEventPublisher {

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private ObjectMapper objectMapper;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDomainEvent(Object event) {
        try {
            if (event instanceof OrderEvent) {
                handleOrderEvent((OrderEvent) event);
            }
        } catch (Exception e) {
            // Log error
            e.printStackTrace();
        }
    }

    private void handleOrderEvent(OrderEvent event) throws Exception {
        OutboxMessage message = new OutboxMessage();
        message.setAggregateType("Order");
        message.setAggregateId(event.getOrderId().toString());
        message.setEventType(event.getClass().getSimpleName());
        message.setPayload(objectMapper.writeValueAsString(event));
        message.setCreatedAt(LocalDateTime.now());
        message.setProcessed(false);
        message.setRetryCount(0);

        outboxService.saveMessage(message);
    }

    // Marker interface for Order events
    private interface OrderEvent {
        OrderId getOrderId();
    }
}