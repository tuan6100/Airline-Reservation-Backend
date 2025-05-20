package vn.edu.hust.application.dto.query;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatReservationDTO {
    private Long seatId;
    private Long flightId;
    private String seatClassName;
    private Double price;
    private String currency;

}
