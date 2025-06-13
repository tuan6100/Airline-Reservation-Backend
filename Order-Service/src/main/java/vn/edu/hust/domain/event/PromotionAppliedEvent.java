package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.valueobj.PromotionObj;

public record PromotionAppliedEvent(
        Long orderId,
        PromotionObj promotion
) {
}
