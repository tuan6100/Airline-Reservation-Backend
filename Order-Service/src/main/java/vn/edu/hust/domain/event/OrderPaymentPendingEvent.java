package vn.edu.hust.domain.event;

import java.time.LocalDateTime;

public record OrderPaymentPendingEvent(
        Long orderId,
        LocalDateTime pendingAt
) {}
