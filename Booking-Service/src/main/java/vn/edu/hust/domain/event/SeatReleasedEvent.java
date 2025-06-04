package vn.edu.hust.domain.event;

public record SeatReleasedEvent(Long seatId, Long flightId) {}
