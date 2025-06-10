package vn.edu.hust.application.dto.query;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class GetAvailableTicketsQuery {
    private Long flightId;
    private LocalDateTime flightDepartureTime;
    private Long seatId;
}
