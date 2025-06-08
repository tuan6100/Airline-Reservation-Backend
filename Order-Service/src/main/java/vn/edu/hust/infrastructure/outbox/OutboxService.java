package vn.edu.hust.infrastructure.outbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class OutboxService {
    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @Autowired
    private OutboxProcessor outboxProcessor;

    @Scheduled(fixedRate = 5000)
    public void processOutbox() {
        List<OutboxMessage> messages = outboxMessageRepository.findByProcessedFalseOrderByCreatedAtAsc(
                PageRequest.of(0, 10));

        for (OutboxMessage message : messages) {
            outboxProcessor.processMessage(message);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveMessage(OutboxMessage message) {
        outboxMessageRepository.save(message);
    }
}