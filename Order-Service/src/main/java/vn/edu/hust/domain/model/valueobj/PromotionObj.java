package vn.edu.hust.domain.model.valueobj;

import vn.edu.hust.domain.model.enumeration.DiscountType;

public record PromotionObj(
        Long promotionId,
        Long discount,
        DiscountType discountType
) {
    public PromotionObj {
        if (discountType.equals(DiscountType.COUPON) && discount > 100) {
            throw new IllegalArgumentException("Discount must be less or equal 100");
        }
    }
}
