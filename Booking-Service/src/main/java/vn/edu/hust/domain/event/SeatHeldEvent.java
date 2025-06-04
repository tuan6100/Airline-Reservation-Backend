package vn.edu.hust.domain.event;

import java.time.LocalDateTime;

public record SeatHeldEvent(
        Long seatId,
        Long flightId,
        Long customerId,
        LocalDateTime holdUntil
) {}