package vn.edu.hust.domain.model.valueobj;

import java.util.Objects;

public record SeatReservation(SeatId seatId, FlightId flightId, SeatClass seatClass, Money price) {
    // Validation logic in constructor
    public SeatReservation {
        Objects.requireNonNull(seatId, "Seat ID cannot be null");
        Objects.requireNonNull(flightId, "Flight ID cannot be null");
        Objects.requireNonNull(seatClass, "Seat class cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");
    }
}