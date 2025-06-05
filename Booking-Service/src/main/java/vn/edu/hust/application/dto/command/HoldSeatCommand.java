package vn.edu.hust.application.dto.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
public class HoldSeatCommand {
    @TargetAggregateIdentifier
    private Long seatId;
    private Long customerId;
    private Long flightId;
    private Integer holdDurationMinutes;
}