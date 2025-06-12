package vn.edu.hust.application.dto.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import vn.edu.hust.application.dto.query.TicketBookedDTO;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class AddOrderItemCommand {
    @TargetAggregateIdentifier
    private Long orderId;
    private List<TicketBookedDTO> items = new ArrayList<>();
}
