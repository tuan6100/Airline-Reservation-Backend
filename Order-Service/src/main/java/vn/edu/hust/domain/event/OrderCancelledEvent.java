package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.OrderId;

import java.time.LocalDateTime;

public record OrderCancelledEvent(
        OrderId orderId,
        BookingId bookingId,
        String reason,
        LocalDateTime cancelledAt
) {}
