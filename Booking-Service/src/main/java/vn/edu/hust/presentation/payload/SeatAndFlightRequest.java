package vn.edu.hust.presentation.payload;

import java.time.LocalDateTime;
import java.util.List;

public record SeatAndFlightRequest(
        List<Long> seatIds,
        Long flightId,
        LocalDateTime flightDepartureTime
) {
    public SeatAndFlightRequest(List<Long> seatIds, Long flightId, String flightDepartureTime) {
        this(seatIds, flightId, LocalDateTime.parse(flightDepartureTime));
    }
}