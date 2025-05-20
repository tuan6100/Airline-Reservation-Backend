package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.SeatId;

public record SeatAddedToBookingEvent(BookingId bookingId, SeatId seatId) {}