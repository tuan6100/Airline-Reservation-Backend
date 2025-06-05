package vn.edu.hust.infrastructure.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.enumeration.BookingStatus;
import vn.edu.hust.infrastructure.entity.BookingEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingEntity, String> {
    List<BookingEntity> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime expiresAt);
    List<BookingEntity> findByCustomerId(Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BookingEntity b WHERE b.bookingId = :bookingId")
    BookingEntity findByIdWithLock(@Param("bookingId") String bookingId);


    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT b FROM BookingEntity b WHERE b.bookingId = :bookingId")
    Optional<BookingEntity> findByIdWithOptimisticLock(@Param("bookingId") String bookingId);
}