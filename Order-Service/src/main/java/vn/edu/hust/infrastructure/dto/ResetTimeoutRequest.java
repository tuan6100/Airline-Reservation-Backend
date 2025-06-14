package vn.edu.hust.infrastructure.dto;

public record ResetTimeoutRequest(
        Long ticketId,
        Long seatId
) {
}
