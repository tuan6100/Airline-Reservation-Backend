package vn.edu.hust.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RemoveItemFromOrderCommand {
    @TargetAggregateIdentifier
    private Long orderId;
    private Long ticketId;
}
