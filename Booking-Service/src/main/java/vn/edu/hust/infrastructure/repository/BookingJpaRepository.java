package vn.edu.hust.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hust.infrastructure.entity.BookingEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingEntity, String> {
    List<BookingEntity> findByStatusAndExpiresAtBefore(String status, LocalDateTime expiresAt);
    List<BookingEntity> findByCustomerId(Long customerId);
}
