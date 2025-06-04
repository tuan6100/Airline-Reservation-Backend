package vn.edu.hust.application.dto.query;


import lombok.AllArgsConstructor;
import lombok.Data;
import vn.edu.hust.domain.model.enumeration.SeatStatus;

@Data
@AllArgsConstructor
public class SeatDTO {
    private Long seatId;
    private Long flightId;
    private Long seatClassId;
    private SeatStatus status;
}
