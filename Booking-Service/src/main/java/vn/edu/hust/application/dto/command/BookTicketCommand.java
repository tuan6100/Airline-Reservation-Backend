package vn.edu.hust.application.dto.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Data
@NoArgsConstructor
public class BookTicketCommand {
    @TargetAggregateIdentifier
    private List<Long> ticketIds;
    private Long customerId;
    private String bookingId;
}
