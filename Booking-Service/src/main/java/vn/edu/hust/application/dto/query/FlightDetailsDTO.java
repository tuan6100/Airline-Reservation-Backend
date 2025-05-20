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
    private String flightNumber;
    private String departureAirportCode;
    private String departureAirportName;
    private String arrivalAirportCode;
    private String arrivalAirportName;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String aircraftType;
    private String airline;
    private int availableEconomySeats;
    private int availableBusinessSeats;
    private int availableFirstClassSeats;
    private double baseEconomyPrice;
    private double baseBusinessPrice;
    private double baseFirstClassPrice;
}
