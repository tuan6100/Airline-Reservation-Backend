package vn.edu.hust.domain.event;

import vn.edu.hust.application.enumeration.CurrencyUnit;
import vn.edu.hust.domain.model.enumeration.OrderStatus;

import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long customerId,
        String bookingId,
        OrderStatus status,
        Long totalPrice,
        CurrencyUnit currency,
        LocalDateTime createdAt
) {}


