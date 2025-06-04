package vn.edu.hust.application.dto.query;

import lombok.Data;
import vn.edu.hust.domain.model.enumeration.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TicketSummaryDTO {
    private Long ticketId;
    private UUID ticketCode;
    private Long flightId;
    private String flightNumber;
    private Long seatId;
    private String seatCode;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime departureTime;
    private String departureAirport;
    private String arrivalAirport;
}
