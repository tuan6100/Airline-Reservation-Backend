package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.Money;
import vn.edu.hust.domain.model.valueobj.OrderId;

public record OrderConfirmedEvent(
        OrderId orderId,
        BookingId bookingId,
        Money totalAmount
) {}
