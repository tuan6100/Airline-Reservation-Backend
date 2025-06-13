package vn.edu.hust.application.dto.query;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.hust.domain.model.enumeration.DiscountType;

@Getter
@Setter
@NoArgsConstructor
public class PromotionDTO {
    Long promotionId;
    String promotionName;
    Integer discount;
    DiscountType discount_type;
    Integer expireAt;
}
