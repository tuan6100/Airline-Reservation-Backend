package vn.edu.hust.application.dto.command;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeatSelectionDTO {
    private Long seatId;
    private BigDecimal amount;
    private String currency;

}
