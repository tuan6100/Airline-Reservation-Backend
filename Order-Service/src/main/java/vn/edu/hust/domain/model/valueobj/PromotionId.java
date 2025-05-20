package vn.edu.hust.domain.model.valueobj;

public record PromotionId(Long value) {
    // Allow null for no promotion

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }
}
