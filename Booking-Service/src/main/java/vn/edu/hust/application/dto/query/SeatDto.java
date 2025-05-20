package vn.edu.hust.application.dto.query;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatDTO {
    private Long seatId;
    private Long flightId;
    private Long seatClassId;
    private String status;
}
