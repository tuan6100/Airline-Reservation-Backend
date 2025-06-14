package vn.edu.hust.infrastructure.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class OutboxProcessor {
    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processMessage(OutboxMessage message) {
        try {
            String topic = determineTopicForEvent(message.getEventType());
            kafkaTemplate.send(topic, message.getAggregateId(), message.getPayload());
            message.setProcessed(true);
            message.setProcessedAt(LocalDateTime.now());
            outboxMessageRepository.save(message);
        } catch (Exception e) {
            message.setRetryCount(message.getRetryCount() + 1);
            if (message.getRetryCount() >= 5) {
                message.setProcessed(true);
                message.setProcessedAt(LocalDateTime.now());
                OutboxProcessor.log.info("Max retries reached for outbox message: {}, event: {}", message.getId(), message.getEventType());
            }
            outboxMessageRepository.save(message);
        }
    }

    private String determineTopicForEvent(String eventType) {
        return switch (eventType) {
            case "OrderCreatedEvent" -> "order-events.created";
            case "OrderConfirmedEvent" -> "order-events.confirmed";
            case "OrderCancelledEvent" -> "order-events.cancelled";
            case "OrderStatusChangedEvent" -> "order-events.status-changed";
            default -> "order-events";
        };
    }
}