package vn.edu.hust.infrastructure.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.infrastructure.outbox.OutboxHelper;


@Component
public class KafkaEventPublisher {

    @Autowired
    private OutboxHelper outboxHelper;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            outboxHelper.saveToOutbox(event.orderId().toString(), "OrderCreatedEvent", event);
        } catch (Exception e) {
            System.err.println("Failed to save OrderCreatedEvent to outbox: " + e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        try {
            outboxHelper.saveToOutbox(event.orderId().toString(), "OrderConfirmedEvent", event);
        } catch (Exception e) {
            System.err.println("Failed to save OrderConfirmedEvent to outbox: " + e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        try {
            outboxHelper.saveToOutbox(event.orderId().toString(), "OrderCancelledEvent", event);
        } catch (Exception e) {
            System.err.println("Failed to save OrderCancelledEvent to outbox: " + e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        try {
            outboxHelper.saveToOutbox(event.orderId().toString(), "OrderStatusChangedEvent", event);
        } catch (Exception e) {
            System.err.println("Failed to save OrderStatusChangedEvent to outbox: " + e.getMessage());
        }
    }
}