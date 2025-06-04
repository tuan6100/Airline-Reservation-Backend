package vn.edu.hust.application.dto.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
public class HoldTicketCommand {
    @TargetAggregateIdentifier
    private Long ticketId;
    private Long customerId;
    private Integer holdDurationMinutes;
}
