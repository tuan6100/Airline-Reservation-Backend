package vn.edu.hust.domain.event;

import vn.edu.hust.application.enumeration.CurrencyUnit;
import vn.edu.hust.domain.model.enumeration.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long customerId,
        String bookingId,
        OrderStatus status,
        BigDecimal totalAmount,
        CurrencyUnit currency,
        LocalDateTime createdAt
) {}


