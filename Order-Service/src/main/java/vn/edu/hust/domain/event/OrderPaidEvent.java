package vn.edu.hust.domain.event;

import vn.edu.hust.application.enumeration.CurrencyUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPaidEvent(
        Long orderId,
        Long paymentId,
        Long totalPrice,
        CurrencyUnit currency,
        LocalDateTime paidAt
) {}
