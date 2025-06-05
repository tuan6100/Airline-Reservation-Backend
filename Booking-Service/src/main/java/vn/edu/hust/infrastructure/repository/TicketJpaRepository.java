package vn.edu.hust.infrastructure.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
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

    @Query("SELECT t FROM TicketEntity t WHERE t.flightId = :flightId AND t.status = 0")
    List<TicketEntity> findAvailableByFlightId(@Param("flightId") Long flightId);

    TicketEntity findByTicketId(Long ticketId);
    List<TicketEntity> findByBookingId(String bookingId);
    List<TicketEntity> findByStatus(TicketStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketEntity t WHERE t.ticketId = :ticketId")
    TicketEntity findByIdWithPessimisticLock(@Param("ticketId") Long ticketId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketEntity t WHERE t.ticketId = :ticketId AND t.status = :expectedStatus")
    Optional<TicketEntity> findByIdAndStatusWithLock(
            @Param("ticketId") Long ticketId,
            @Param("expectedStatus") vn.edu.hust.domain.model.enumeration.TicketStatus expectedStatus
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
}