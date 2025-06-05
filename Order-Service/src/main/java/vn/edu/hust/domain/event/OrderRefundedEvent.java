package vn.edu.hust.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderRefundedEvent(
        Long orderId,
        BigDecimal refundAmount,
        String currency,
        LocalDateTime refundedAt
) {}
