package vn.edu.hust.domain.event;

import java.math.BigDecimal;

public record OrderItemAddedEvent(
        Long orderId,
        Long ticketId,
        Long flightId,
        Long seatId,
        BigDecimal price,
        String currency,
        String description
) {}