package vn.edu.hust.domain.model.entity;

import lombok.Data;
import vn.edu.hust.domain.model.valueobj.*;

import java.util.Objects;

/**
 * Order Item Entity
 */
@Data
public class OrderItem {
    private Long id;
    private TicketId ticketId;
    private FlightId flightId;
    private SeatId seatId;
    private Money price;
    private String description;

    // Private constructor for creation via static methods
    private OrderItem() {
    }

    /**
     * Create a new order item
     */
    public static OrderItem create(TicketId ticketId, FlightId flightId, SeatId seatId, Money price, String description) {
        Objects.requireNonNull(ticketId, "Ticket ID cannot be null");
        Objects.requireNonNull(flightId, "Flight ID cannot be null");
        Objects.requireNonNull(seatId, "Seat ID cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");

        OrderItem item = new OrderItem();
        item.ticketId = ticketId;
        item.flightId = flightId;
        item.seatId = seatId;
        item.price = price;
        item.description = description;

        return item;
    }

    // Equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(ticketId, orderItem.ticketId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId);
    }
}