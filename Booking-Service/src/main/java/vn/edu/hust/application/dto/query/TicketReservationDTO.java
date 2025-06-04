package vn.edu.hust.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketReservationDTO {
    private Long ticketId;
    private Long seatId;
    private String seatCode;
    private String seatClassName;
    private Double price;
    private String currency;
}