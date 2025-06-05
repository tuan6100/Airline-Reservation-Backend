package vn.edu.hust.application.dto.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
public class AddOrderItemCommand {
    @TargetAggregateIdentifier
    private Long orderId;
    private Long ticketId;
    private Long flightId;
    private Long seatId;
    private Double price;
    private String currency;
    private String description;
}
