package vn.edu.hust.domain.model.valueobj;

public record SeatReservation(
        Long seatId,
        Long flightId,
        Long seatClassId,
        Double amount,
        String currency
) {}