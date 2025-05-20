package vn.edu.hust.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class BookingDTO {
    private String bookingId;
    private Long customerId;
    private List<SeatReservationDTO> seats;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

}
