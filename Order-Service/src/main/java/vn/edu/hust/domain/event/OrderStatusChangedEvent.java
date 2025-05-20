package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.enumeration.OrderStatus;
import vn.edu.hust.domain.model.valueobj.OrderId;

import java.time.LocalDateTime;

public record OrderStatusChangedEvent(
        OrderId orderId,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        LocalDateTime updatedAt
) {}
