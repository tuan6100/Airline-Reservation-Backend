package vn.edu.hust.domain.model.valueobj;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TicketReservation {
    private Long ticketId;
    private Long seatId;
    private Double price;
    private String currency;

    public TicketReservation(Long ticketId, Long seatId, Double price, String currency) {
        if (ticketId == null || ticketId <= 0) {
            throw new IllegalArgumentException("Ticket ID must be positive");
        }
        if (seatId == null || seatId <= 0) {
            throw new IllegalArgumentException("Seat ID must be positive");
        }
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }
        this.ticketId = ticketId;
        this.seatId = seatId;
        this.price = price;
        this.currency = currency.toUpperCase();
    }
}