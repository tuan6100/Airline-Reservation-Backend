package vn.edu.hust.infrastructure.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for OutboxMessage entities
 */
@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {
    
    /**
     * Find unprocessed messages ordered by creation time
     * 
     * @param pageable pagination information
     * @return list of unprocessed messages
     */
    List<OutboxMessage> findByProcessedFalseOrderByCreatedAtAsc(Pageable pageable);
}