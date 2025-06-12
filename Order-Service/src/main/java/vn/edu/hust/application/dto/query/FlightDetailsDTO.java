package vn.edu.hust.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDetailsDTO {
    private Long flightId;
    private String airlineName;
    private String aircraftName;
    private LocalDateTime departureTime;
    private String departureAirport;
    private String arrivalAirport;
}
