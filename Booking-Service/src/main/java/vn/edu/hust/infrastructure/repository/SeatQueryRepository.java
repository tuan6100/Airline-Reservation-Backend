package vn.edu.hust.infrastructure.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hust.application.dto.query.SeatDTO;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class SeatQueryRepository {

    @Autowired
    private SeatJpaRepository seatRepository;

    public List<SeatDTO> findAvailableByFlightId(Long flightId) {
        return seatRepository.findAvailableByAircraftId(flightId)
                .stream()
                .map(projection -> new SeatDTO(
                        projection.getSeatId(),
                        flightId,
                        projection.getSeatClassId(),
                        projection.getStatus()
                ))
                .collect(Collectors.toList());
    }
}