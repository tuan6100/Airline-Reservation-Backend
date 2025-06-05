package vn.edu.hust.domain.model.valueobj;

import java.math.BigDecimal;

public record OrderItem(
        Long id,
        Long ticketId,
        Long flightId,
        Long seatId,
        BigDecimal price,
        String currency,
        String description
) {
    public OrderItem(Long ticketId, Long flightId, Long seatId, BigDecimal price, String currency, String description) {
        this(null, ticketId, flightId, seatId, price, currency, description);
    }

    public static OrderItem create(TicketId ticketId, FlightId flightId, SeatId seatId, Money price, String description) {
        return new OrderItem(
                null,
                ticketId.value(),
                flightId.value(),
                seatId.value(),
                price.getAmount(),
                price.getCurrency().getCurrencyCode(),
                description
        );
    }
    public static OrderItem withId(Long id, Long ticketId, Long flightId, Long seatId, BigDecimal price, String currency, String description) {
        return new OrderItem(id, ticketId, flightId, seatId, price, currency, description);
    }
    public TicketId getTicketId() { return new TicketId(ticketId); }
    public FlightId getFlightId() { return new FlightId(flightId); }
    public SeatId getSeatId() { return new SeatId(seatId); }
    public Money getPrice() {
        return new Money(price, java.util.Currency.getInstance(currency));
    }
}