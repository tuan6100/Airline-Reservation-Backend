package vn.edu.hust.domain.event;

import vn.edu.hust.domain.model.enumeration.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusChangedEvent(
        Long orderId,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        LocalDateTime updatedAt
) {}
