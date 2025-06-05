package vn.edu.hust.domain.model.valueobj;

import java.math.BigDecimal;

public record OrderItem(
        Long ticketId,
        Long flightId,
        Long seatId,
        BigDecimal price,
        String currency,
        String description
) {}
