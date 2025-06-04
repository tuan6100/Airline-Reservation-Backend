package vn.edu.hust.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.hust.domain.model.enumeration.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private String bookingId;
    private Long customerId;
    private List<SeatReservationDTO> seats;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

}
