package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.valueobj.BookingId;

public record BookingConfirmedEvent(BookingId bookingId) {}