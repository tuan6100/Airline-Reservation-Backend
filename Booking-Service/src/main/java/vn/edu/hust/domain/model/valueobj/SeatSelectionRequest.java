package vn.edu.hust.domain.model.valueobj;

import java.util.Objects;

public record SeatSelectionRequest(SeatId seatId, SeatClassId seatClassId, Money price) {

    public SeatSelectionRequest {
        Objects.requireNonNull(seatId, "Seat ID cannot be null");
        Objects.requireNonNull(seatClassId, "Seat Class ID cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");

        if (price.amount().signum() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }

    public SeatSelectionRequest(SeatId seatId, Money price) {
        this(seatId, new SeatClassId(1L), price); // Default to Economy
    }

    public SeatSelectionRequest withPrice(Money newPrice) {
        return new SeatSelectionRequest(this.seatId, this.seatClassId, newPrice);
    }

    @Override
    public String toString() {
        return "SeatSelectionRequest{" +
                "seatId=" + seatId +
                ", seatClassId=" + seatClassId +
                ", price=" + price +
                '}';
    }
}