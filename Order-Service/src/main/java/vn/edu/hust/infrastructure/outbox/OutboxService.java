package vn.edu.hust.infrastructure.outbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to handle outbox pattern
 */
@Service
public class OutboxService {

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void saveMessage(OutboxMessage message) {
        outboxMessageRepository.save(message);
    }

    @Scheduled(fixedRate = 5000) // Run every 5 seconds
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOutbox() {
        List<OutboxMessage> messages = outboxMessageRepository.findByProcessedFalseOrderByCreatedAtAsc(
                PageRequest.of(0, 10));

        for (OutboxMessage message : messages) {
            try {
                // Determine topic based on event type
                String topic = determineTopicForEvent(message.getEventType());

                // Send to Kafka
                kafkaTemplate.send(topic, message.getAggregateId(), message.getPayload());

                // Mark as processed
                message.setProcessed(true);
                message.setProcessedAt(LocalDateTime.now());
                outboxMessageRepository.save(message);
            } catch (Exception e) {
                // Increment retry count
                message.setRetryCount(message.getRetryCount() + 1);
                outboxMessageRepository.save(message);

                // Log error
                e.printStackTrace();
            }
        }
    }

    private String determineTopicForEvent(String eventType) {
        switch (eventType) {
            case "OrderCreatedEvent":
                return "order-events.created";
            case "OrderConfirmedEvent":
                return "order-events.confirmed";
            case "OrderCancelledEvent":
                return "order-events.cancelled";
            case "OrderStatusChangedEvent":
                return "order-events.status-changed";
            default:
                return "order-events";
        }
    }
}