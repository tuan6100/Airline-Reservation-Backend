package vn.edu.hust.application.dto.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
public class MarkOrderPaidCommand {
    @TargetAggregateIdentifier
    private Long orderId;
    private Long paymentId;
}
