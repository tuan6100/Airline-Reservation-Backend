package vn.edu.hust.application.dto.query;

import lombok.Data;
import java.util.List;

@Data
public class TicketAvailabilityDTO {
    private Long flightId;

    // Economy class
    private Integer economyCount;
    private List<TicketSearchDTO> economyTickets;

    // Business class
    private Integer businessCount;
    private List<TicketSearchDTO> businessTickets;

    // First class
    private Integer firstClassCount;
    private List<TicketSearchDTO> firstClassTickets;

    // Total
    public Integer getTotalAvailable() {
        return economyCount + businessCount + firstClassCount;
    }
}