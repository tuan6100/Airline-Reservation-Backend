package vn.edu.hust.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hust.infrastructure.entity.SeatEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatJpaRepository extends JpaRepository<SeatEntity, Long> {
    List<SeatEntity> findByFlightId(Long flightId);
    List<SeatEntity> findByFlightIdAndStatus(Long flightId, String status);
    List<SeatEntity> findByStatusAndHoldUntilBefore(String status, LocalDateTime holdUntil);
}