package vn.edu.hust.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SeatAndFlightDTO {
    private Long seatId;
    private Long flightId;
    private LocalDateTime flightDepartureTime;
}
