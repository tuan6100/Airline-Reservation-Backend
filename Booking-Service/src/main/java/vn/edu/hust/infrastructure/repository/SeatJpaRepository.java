package vn.edu.hust.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hust.infrastructure.entity.SeatEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatJpaRepository extends JpaRepository<SeatEntity, Long> {
    List<SeatEntity> findByAircraftId(Long aircraftId);

    @Query("SELECT s FROM SeatEntity s WHERE s.aircraftId = :aircraftId AND s.isAvailable = true")
    List<SeatEntity> findAvailableByAircraftId(@Param("aircraftId") Long aircraftId);

    @Query("SELECT s FROM SeatEntity s WHERE s.isAvailable = false AND s.holdUntil < :now")
    List<SeatEntity> findExpiredHolds(@Param("now") LocalDateTime now);
}