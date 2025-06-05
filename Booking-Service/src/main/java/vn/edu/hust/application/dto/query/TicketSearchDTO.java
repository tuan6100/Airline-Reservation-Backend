package vn.edu.hust.application.dto.query;

import lombok.Data;
import vn.edu.hust.domain.model.enumeration.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TicketSearchDTO {
    private Long ticketId;
    private UUID ticketCode;
    private Long flightId;
    private LocalDateTime flightDepartureTime;
    private TicketStatus status;
    private LocalDateTime createdAt;

    // Seat information
    private Long seatId;
    private String seatCode;
    private Long seatClassId;
    private String seatClassName;

    // Pricing information
    private Double price;
    private String currency;

    // Availability status
    private Boolean isAvailable;
    private LocalDateTime holdUntil;
    private Long heldByCustomerId;
}