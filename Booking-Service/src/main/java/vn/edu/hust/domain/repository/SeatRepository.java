package vn.edu.hust.domain.repository;

import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.aggregate.Seat;
import vn.edu.hust.domain.model.valueobj.FlightId;
import vn.edu.hust.domain.model.valueobj.SeatId;

import java.util.List;

@Repository
public interface SeatRepository {
    Seat findById(SeatId seatId);
    List<Seat> findByFlightId(FlightId flightId);
    List<Seat> findAvailableByFlightId(FlightId flightId);
    List<Seat> findExpiredHolds();
    void save(Seat seat);
}
