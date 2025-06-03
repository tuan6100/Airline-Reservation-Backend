package vn.edu.hust.domain.repository;

import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.aggregate.Seat;
import vn.edu.hust.domain.model.valueobj.AircraftId;
import vn.edu.hust.domain.model.valueobj.SeatId;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatRepository {
    Seat findById(SeatId seatId);
    Seat save(Seat seat);
    List<Seat> findByAircraftId(AircraftId aircraftId);
    List<Seat> findAvailableByAircraftId(AircraftId aircraftId);
    List<Seat> findExpiredHolds();
}