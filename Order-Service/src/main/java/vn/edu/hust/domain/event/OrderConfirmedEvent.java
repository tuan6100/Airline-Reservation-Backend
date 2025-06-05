package vn.edu.hust.domain.event;

import java.math.BigDecimal;

public record OrderConfirmedEvent(
        Long orderId,
        String bookingId,
        BigDecimal totalAmount,
        String currency
) {}
