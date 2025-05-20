package vn.edu.hust.infrastructure.dto;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Setter
public class InitiatePaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String currency;
    private Long customerId;

}
