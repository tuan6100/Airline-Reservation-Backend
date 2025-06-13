package vn.edu.hust.domain.event;

import vn.edu.hust.application.enumeration.CurrencyUnit;

public record OrderConfirmedEvent(
        Long orderId,
        String bookingId,
        Long totalPrice,
        CurrencyUnit currency
) {}
