package vn.edu.hust.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.infrastructure.outbox.OutboxMessage;
import vn.edu.hust.infrastructure.outbox.OutboxService;

import java.time.LocalDateTime;


@Component
public class KafkaEventPublisher {

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private ObjectMapper objectMapper;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            OutboxMessage message = new OutboxMessage();
            message.setAggregateType("Order");
            message.setAggregateId(event.orderId().toString());
            message.setEventType("OrderCreatedEvent");
            message.setPayload(objectMapper.writeValueAsString(event));
            message.setCreatedAt(LocalDateTime.now());
            message.setProcessed(false);
            message.setRetryCount(0);

            outboxService.saveMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        try {
            OutboxMessage message = new OutboxMessage();
            message.setAggregateType("Order");
            message.setAggregateId(event.orderId().toString());
            message.setEventType("OrderConfirmedEvent");
            message.setPayload(objectMapper.writeValueAsString(event));
            message.setCreatedAt(LocalDateTime.now());
            message.setProcessed(false);
            message.setRetryCount(0);

            outboxService.saveMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        try {
            OutboxMessage message = new OutboxMessage();
            message.setAggregateType("Order");
            message.setAggregateId(event.orderId().toString());
            message.setEventType("OrderCancelledEvent");
            message.setPayload(objectMapper.writeValueAsString(event));
            message.setCreatedAt(LocalDateTime.now());
            message.setProcessed(false);
            message.setRetryCount(0);

            outboxService.saveMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        try {
            OutboxMessage message = new OutboxMessage();
            message.setAggregateType("Order");
            message.setAggregateId(event.orderId().toString());
            message.setEventType("OrderStatusChangedEvent");
            message.setPayload(objectMapper.writeValueAsString(event));
            message.setCreatedAt(LocalDateTime.now());
            message.setProcessed(false);
            message.setRetryCount(0);

            outboxService.saveMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}