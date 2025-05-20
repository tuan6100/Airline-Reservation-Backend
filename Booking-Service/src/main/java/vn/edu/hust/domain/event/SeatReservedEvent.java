package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.valueobj.CustomerId;
import vn.edu.hust.domain.model.valueobj.FlightId;
import vn.edu.hust.domain.model.valueobj.SeatId;

public record SeatReservedEvent(SeatId seatId, FlightId flightId, CustomerId customerId) {}