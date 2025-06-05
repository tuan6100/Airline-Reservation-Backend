package vn.edu.hust.application.dto.query;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Order Summary
 */
@Data
public class OrderSummaryDTO {
    private Long orderId;
    private Long customerId;
    private String bookingId;
    private String status;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime createdAt;
    private int itemCount;
}