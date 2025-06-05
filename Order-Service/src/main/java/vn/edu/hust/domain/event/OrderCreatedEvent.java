package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.enumeration.OrderStatus;
import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.CustomerId;
import vn.edu.hust.domain.model.valueobj.Money;
import vn.edu.hust.domain.model.valueobj.OrderId;
import vn.edu.hust.domain.model.valueobj.PromotionId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long orderId,
        Long customerId,
        String bookingId,
        Long promotionId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        LocalDateTime createdAt
) {}


