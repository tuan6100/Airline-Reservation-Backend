package vn.edu.hust.domain.event;

import java.time.LocalDateTime;

public record SeatHoldExpiredEvent(
        Long seatId,
        Long customerId,
        String seatCode,
        Long flightId,
        LocalDateTime expiredAt,
        LocalDateTime originalHoldTime
) {}