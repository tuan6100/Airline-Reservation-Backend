package vn.edu.hust.application.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
public class TicketBookedDTO {
    private UUID ticketCode;
    private String seatCode;
    private Long price;
    private FlightDetailsDTO flightDetails;
}
