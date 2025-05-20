package vn.edu.hust.domain.model.valueobj;

public record SeatId(Long value) {
    public SeatId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Seat ID must be positive");
        }
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }
}
