package vn.edu.hust.domain.model.valueobj;

public record TicketId(Long value) {
    public TicketId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Ticket ID must be positive");
        }
    }
}