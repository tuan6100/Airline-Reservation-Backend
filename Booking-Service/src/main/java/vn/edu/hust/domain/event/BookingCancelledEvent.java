package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.enumeration.CancellationReason;

public record BookingCancelledEvent(String bookingId, CancellationReason reason) {}