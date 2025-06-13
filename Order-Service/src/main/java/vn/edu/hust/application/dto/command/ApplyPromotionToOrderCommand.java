package vn.edu.hust.application.dto.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import vn.edu.hust.domain.model.valueobj.PromotionObj;

@Data
@NoArgsConstructor
public class ApplyPromotionToOrderCommand {
    @TargetAggregateIdentifier
    Long orderId;
    PromotionObj promotion;
}
