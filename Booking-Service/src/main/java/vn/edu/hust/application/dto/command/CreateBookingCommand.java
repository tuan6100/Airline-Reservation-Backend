package vn.edu.hust.application.dto.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class CreateBookingCommand {
    @TargetAggregateIdentifier
    private String bookingId;
    private Long customerId;
    private Long flightId;
    private LocalDateTime flightDepartureTime;
    private List<TicketSelectionRequest> ticketSelections;
    private String currency;

    @Data
    @NoArgsConstructor
    public static class TicketSelectionRequest {
        private Long ticketId;
        private Long seatId;
        private Long seatClassId;
        private Double price;
        private String currency;
    }
}