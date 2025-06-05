package vn.edu.hust.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPaidEvent(
        Long orderId,
        Long paymentId,
        BigDecimal amount,
        String currency,
        LocalDateTime paidAt
) {}
