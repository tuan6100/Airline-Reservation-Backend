package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.enumeration.CancellationReason;
import vn.edu.hust.domain.model.valueobj.BookingId;

public record BookingCancelledEvent(BookingId bookingId, CancellationReason reason) {}