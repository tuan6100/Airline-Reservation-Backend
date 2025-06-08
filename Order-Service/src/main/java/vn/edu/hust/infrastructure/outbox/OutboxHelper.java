package vn.edu.hust.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class OutboxHelper {
    @Autowired
    private OutboxService outboxService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveToOutbox(String aggregateId, String eventType, Object event) {
        try {
            OutboxMessage message = new OutboxMessage();
            message.setAggregateType("Order");
            message.setAggregateId(aggregateId);
            message.setEventType(eventType);
            message.setPayload(objectMapper.writeValueAsString(event));
            message.setCreatedAt(LocalDateTime.now());
            message.setProcessed(false);
            message.setRetryCount(0);
            outboxService.saveMessage(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }

}
