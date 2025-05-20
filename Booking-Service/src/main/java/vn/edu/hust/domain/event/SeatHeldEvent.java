package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.valueobj.CustomerId;
import vn.edu.hust.domain.model.valueobj.FlightId;
import vn.edu.hust.domain.model.valueobj.SeatId;

import java.time.LocalDateTime;

public record SeatHeldEvent(SeatId seatId, FlightId flightId, CustomerId customerId,
                            LocalDateTime holdUntil) {}