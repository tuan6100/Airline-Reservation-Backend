package vn.edu.hust.infrastructure.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    @Query("UPDATE BookingEntity b SET b.status = :newStatus, b.updatedAt = :updateTime " +
            "WHERE b.bookingId = :bookingId AND b.status = :currentStatus ")
    int updateBookingStatusAtomic(
            @Param("bookingId") String bookingId,
            @Param("currentStatus") BookingStatus currentStatus,
            @Param("newStatus") BookingStatus newStatus,
            @Param("updateTime") LocalDateTime updateTime
    );

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM BookingEntity b WHERE b.bookingId = :bookingId " +
            "AND b.status = 'PENDING' AND b.expiresAt > :currentTime")
    boolean isBookingConfirmable(@Param("bookingId") String bookingId,
                                 @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM BookingEntity b WHERE b.status = 'PENDING' AND b.expiresAt < :currentTime")
    List<BookingEntity> findExpiredPendingBookings(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE BookingEntity b SET b.status = 'EXPIRED', b.updatedAt = :updateTime " +
            "WHERE b.status = 'PENDING' AND b.expiresAt < :currentTime")
    int bulkExpirePendingBookings(@Param("currentTime") LocalDateTime currentTime,
                                  @Param("updateTime") LocalDateTime updateTime);
}