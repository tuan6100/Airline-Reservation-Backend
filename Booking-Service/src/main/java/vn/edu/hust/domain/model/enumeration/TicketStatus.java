package vn.edu.hust.domain.model.enumeration;

import lombok.Getter;

@Getter
public enum TicketStatus {
    AVAILABLE(0),
    HELD(1),
    BOOKED(2),
    CANCELLED(3);

    private final int value;

    TicketStatus(int value) {
        this.value = value;
    }

    public static TicketStatus fromValue(int value) {
        for (TicketStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ticket status: " + value);
    }
}