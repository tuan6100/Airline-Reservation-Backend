package vn.edu.hust.infrastructure.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO from Payment Service
 */
@Data
public class PaymentResponseDTO {
    private String paymentId;
    private Long orderId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String paymentUrl;
    private LocalDateTime expiresAt;
    private String message;
}