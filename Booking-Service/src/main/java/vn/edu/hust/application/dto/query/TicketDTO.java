package vn.edu.hust.application.dto.query;

import lombok.Data;
import vn.edu.hust.domain.model.enumeration.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TicketDTO {
    protected Long ticketId;
    protected UUID ticketCode;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime holdUntil;
    private Long heldByCustomerId;
    private String bookingId;
    private Long seatId;
    protected FlightDetailsDTO flightDetails;
}
