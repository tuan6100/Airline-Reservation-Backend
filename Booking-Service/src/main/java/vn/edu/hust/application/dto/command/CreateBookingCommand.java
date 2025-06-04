package vn.edu.hust.application.dto.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateBookingCommand {
    @TargetAggregateIdentifier
    private String bookingId;
    private Long customerId;
    private List<SeatSelectionRequest> seatSelections;

    @Data
    @NoArgsConstructor
    public static class SeatSelectionRequest {
        private Long seatId;
        private Long seatClassId;
        private Double amount;
        private String currency;
    }
}