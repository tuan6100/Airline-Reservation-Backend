package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.valueobj.SeatReservation;

import java.time.LocalDateTime;
import java.util.Set;

public record BookingCreatedEvent(
        String bookingId,
        Long customerId,
        Set<SeatReservation> seatReservations,
        LocalDateTime expiresAt
) {}