package vn.edu.hust.domain.event;

import java.time.LocalDateTime;

public record OrderCancelledEvent(
        Long orderId,
        String bookingId,
        String reason,
        LocalDateTime cancelledAt
) {}
