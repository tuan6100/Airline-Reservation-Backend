package vn.edu.hust.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hust.infrastructure.entity.TicketEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketJpaRepository extends JpaRepository<TicketEntity, Long> {
    List<TicketEntity> findByFlightId(Long flightId);

    @Query("SELECT t FROM TicketEntity t WHERE t.flightId = :flightId AND t.status = 0")
    List<TicketEntity> findAvailableByFlightId(@Param("flightId") Long flightId);

    TicketEntity findBySeatId(Long seatId);

    @Query("SELECT t FROM TicketEntity t WHERE t.status = 1 AND t.seat.holdUntil < :now")
    List<TicketEntity> findExpiredHolds(@Param("now") LocalDateTime now);
}