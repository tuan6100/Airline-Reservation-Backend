package vn.edu.hust.domain.model.valueobj;

public record SeatDetails(
        SeatId seatId,
        SeatClassId seatClassId,
        AircraftId aircraftId,
        String seatCode,
        Money price
) {}