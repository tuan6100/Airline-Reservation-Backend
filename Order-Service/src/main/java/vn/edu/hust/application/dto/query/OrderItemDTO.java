package vn.edu.hust.application.dto.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Data Transfer Object for OrderItem
 */
@Data
public class OrderItemDTO {
    private Long id;
    private Long ticketId;
    private Long flightId;
    private Long seatId;
    private BigDecimal price;
    private String currency;
    private String description;
}