package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.valueobj.FlightId;
import vn.edu.hust.domain.model.valueobj.SeatId;

public record SeatReleasedEvent(SeatId seatId, FlightId flightId) {}
