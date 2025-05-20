package vn.edu.hust.domain.model.valueobj;


public record FlightId(Long value) {
    public FlightId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Flight ID must be positive");
        }
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }
}
