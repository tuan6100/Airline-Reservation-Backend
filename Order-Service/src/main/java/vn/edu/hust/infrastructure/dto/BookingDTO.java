package vn.edu.hust.infrastructure.dto;

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
