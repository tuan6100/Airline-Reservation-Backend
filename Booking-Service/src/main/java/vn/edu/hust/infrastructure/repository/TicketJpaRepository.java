package vn.edu.hust.infrastructure.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.enumeration.TicketStatus;
import vn.edu.hust.infrastructure.entity.TicketEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketJpaRepository extends JpaRepository<TicketEntity, Long> {

    List<TicketEntity> findByFlightId(Long flightId);

    @Query("SELECT t FROM TicketEntity t " +
            "WHERE t.seat.seatId = :seatId " +
            "AND t.flightId = :flightId " +
            "AND t.flightDepartureTime = :flightDepartureTime " +
            "ORDER BY t.createdAt")
    TicketEntity findTicketsBySeatAndFlightAndTime(
            @Param("seatId") Long seatId,
            @Param("flightId") Long flightId,
            @Param("flightDepartureTime") LocalDateTime flightDepartureTime
    );

    @Query("SELECT t FROM TicketEntity t " +
            "WHERE t.flightId = :flightId " +
            "AND t.flightDepartureTime = :flightDepartureTime " +
            "AND t.seat.seatId = :seatId " +
            "AND t.status = 0")
    TicketEntity findAvailable(
            @Param("flightId") Long flightId,
            @Param("flightDepartureTime") LocalDateTime flightDepartureTime,
            @Param("seatId") Long seatId
    );

    List<TicketEntity> findByBookingId(String bookingId);
    List<TicketEntity> findByStatus(TicketStatus status);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT t FROM TicketEntity t WHERE t.ticketId = :ticketId")
    TicketEntity findByIdWithLock(@Param("ticketId") Long ticketId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT t FROM TicketEntity t WHERE t.ticketId = :ticketId AND t.status = :expectedStatus")
    Optional<TicketEntity> findByIdAndStatusWithLock(
            @Param("ticketId") Long ticketId,
            @Param("expectedStatus") TicketStatus expectedStatus
    );

    @Query("SELECT t FROM TicketEntity t WHERE t.status = 1")
    List<TicketEntity> findCurrentlyHeldTickets();

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT t FROM TicketEntity t WHERE t.flightId = :flightId " +
            "AND t.seat.seatId = :seatId " +
            "AND t.flightDepartureTime = :departureTime " +
            "AND t.status = 0")
    Optional<TicketEntity> findAvailableTicketByFlightAndSeatWithLock(
            @Param("flightId") Long flightId,
            @Param("seatId") Long seatId,
            @Param("departureTime") LocalDateTime departureTime
    );

    @Query("SELECT t FROM TicketEntity t WHERE t.flightId = :flightId " +
            "AND t.seat.seatId = :seatId " +
            "AND t.flightDepartureTime = :departureTime " +
            "AND t.status = 0")
    Optional<TicketEntity> findAvailableTicketByFlightAndSeat(
            @Param("flightId") Long flightId,
            @Param("seatId") Long seatId,
            @Param("departureTime") LocalDateTime departureTime
    );

    @Query("SELECT t FROM TicketEntity t JOIN t.seat s JOIN s.seatClass sc " +
            "WHERE t.flightId = :flightId " +
            "AND sc.id = :seatClassId " +
            "AND t.status = 0 " +
            "ORDER BY s.seatCode")
    List<TicketEntity> findAvailableTicketsByFlightAndSeatClass(
            @Param("flightId") Long flightId,
            @Param("seatClassId") Long seatClassId
    );

    @Query("SELECT t FROM TicketEntity t " +
            "WHERE t.status = 1 " +
            "AND t.updatedAt < :expiredBefore")
    List<TicketEntity> findExpiredHeldTickets(@Param("expiredBefore") LocalDateTime expiredBefore);

    @Modifying
    @Query("UPDATE TicketEntity t SET t.status = 0, t.bookingId = NULL, t.updatedAt = :updateTime " +
            "WHERE t.status = 1 AND t.updatedAt < :expiredBefore")
    int bulkReleaseExpiredHeldTickets(
            @Param("expiredBefore") LocalDateTime expiredBefore,
            @Param("updateTime") LocalDateTime updateTime
    );

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM TicketEntity t " +
            "WHERE t.ticketId = :ticketId AND t.status = 0")
    boolean isTicketAvailable(@Param("ticketId") Long ticketId);

    @Query("SELECT t.ticketId FROM TicketEntity t " +
            "WHERE t.ticketId IN :ticketIds AND t.status = 0")
    List<Long> findAvailableTicketIds(@Param("ticketIds") List<Long> ticketIds);

    @Modifying
    @Query(value = """
        UPDATE tickets SET
            status = 2,
            booking_id = :bookingId,
            updated_at = :updateTime,
            version = version + 1
        WHERE ticket_id = :ticketId
        AND status = 0 OR (status = 1 AND booking_id = :bookingId)
        """, nativeQuery = true)
    int bookTicketAtomic(
            @Param("ticketId") Long ticketId,
            @Param("bookingId") String bookingId,
            @Param("updateTime") LocalDateTime updateTime
    );

    default boolean canBookTicketAtomic(Long ticketId, Long customerId, String bookingId) {
        int updatedRows = bookTicketAtomic(ticketId, bookingId, LocalDateTime.now());
        return updatedRows > 0;
    }

    @Modifying
    @Query("UPDATE TicketEntity t SET " +
            "t.status = :newStatus, " +
            "t.bookingId = :bookingId, " +
            "t.updatedAt = :updateTime " +
            "WHERE t.ticketId = :ticketId " +
            "AND t.status = :currentStatus")
    int updateTicketStatusAtomic(
            @Param("ticketId") Long ticketId,
            @Param("currentStatus") TicketStatus currentStatus,
            @Param("newStatus") TicketStatus newStatus,
            @Param("bookingId") String bookingId,
            @Param("updateTime") LocalDateTime updateTime
    );

    @Modifying
    @Query("UPDATE TicketEntity t SET " +
            "t.status = 0, " +
            "t.bookingId = NULL, " +
            "t.updatedAt = :updateTime " +
            "WHERE t.ticketId = :ticketId " +
            "AND t.status = :currentStatus")
    int releaseTicketAtomic(
            @Param("ticketId") Long ticketId,
            @Param("currentStatus") TicketStatus currentStatus,
            @Param("updateTime") LocalDateTime updateTime
    );

    @Modifying
    @Query("UPDATE TicketEntity t SET " +
            "t.status = 0, " +
            "t.bookingId = NULL, " +
            "t.updatedAt = :updateTime " +
            "WHERE t.bookingId = :bookingId")
    int bulkReleaseTicketsByBooking(
            @Param("bookingId") String bookingId,
            @Param("updateTime") LocalDateTime updateTime
    );
}