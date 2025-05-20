package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.CustomerId;
import vn.edu.hust.domain.model.valueobj.SeatReservation;

import java.time.LocalDateTime;
import java.util.Collection;

public record BookingCreatedEvent(BookingId bookingId, CustomerId customerId,
                                  Collection<SeatReservation> seatReservations,
                                  LocalDateTime expiresAt) {}