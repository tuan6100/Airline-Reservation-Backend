package vn.edu.hust.domain.model.valueobj;

public record OrderId(Long value) {
    public OrderId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }
}
