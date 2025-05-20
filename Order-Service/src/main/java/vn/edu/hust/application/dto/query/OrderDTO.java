package vn.edu.hust.application.dto.query;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Order
 */
@Data
public class OrderDTO {
    private Long orderId;
    private Long customerId;
    private Long bookingId;
    private Long promotionId;
    private String status;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> items = new ArrayList<>();
}