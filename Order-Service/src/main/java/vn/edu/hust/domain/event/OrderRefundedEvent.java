package vn.edu.hust.domain.event;

import vn.edu.hust.application.enumeration.CurrencyUnit;

import java.time.LocalDateTime;

public record OrderRefundedEvent(
        Long orderId,
        Long refundAmount,
        CurrencyUnit currency,
        LocalDateTime refundedAt
) {}
