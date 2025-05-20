package vn.edu.hust.domain.model.valueobj;

public record CustomerId(Long value) {
    public CustomerId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive");
        }
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }
}
