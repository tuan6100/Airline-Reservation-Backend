package vn.edu.hust.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.enumeration.SeatStatus;
import vn.edu.hust.infrastructure.entity.SeatEntity;

import java.util.List;

@Repository
public interface SeatJpaRepository extends JpaRepository<SeatEntity, Long> {
    List<SeatEntity> findByAircraftId(Long aircraftId);
    @Query("SELECT s FROM SeatEntity s WHERE s.aircraftId = :aircraftId AND s.status = :status")
    List<SeatEntity> findByAircraftIdAndStatus(@Param("aircraftId") Long aircraftId, @Param("status") SeatStatus status);
    default List<SeatEntity> findAvailableByAircraftId(Long aircraftId) {
        return findByAircraftIdAndStatus(aircraftId, SeatStatus.AVAILABLE);
    }
    List<SeatEntity> findByStatus(SeatStatus status);
    default List<SeatEntity> findAllAvailable() {
        return findByStatus(SeatStatus.AVAILABLE);
    }
}