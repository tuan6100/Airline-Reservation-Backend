package vn.edu.hust.infrastructure.mapper;

import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.valueobj.OrderItem;
import vn.edu.hust.infrastructure.entity.BookedTicketEntity;
import vn.edu.hust.infrastructure.entity.TicketEntity;

import java.math.BigDecimal;

@Component
public class BookedTicketMapper {

    public OrderItem toOrderItem(BookedTicketEntity bookedTicket) {
        if (bookedTicket == null || bookedTicket.getTicket() == null) {
            return null;
        }
        TicketEntity ticket = bookedTicket.getTicket();
        return new OrderItem(
                ticket.getTicketId(),
                ticket.getFlightId(),
                ticket.getSeat().getSeatId(),
                calculateTicketPrice(ticket),
                "VND",
                "Flight ticket for seat " + (ticket.getSeat() != null ? ticket.getSeat().getSeatCode() : "")
        );
    }

    public BookedTicketEntity toBookedTicketEntity(OrderItem orderItem, Long orderId) {
        if (orderItem == null) {
            return null;
        }
        BookedTicketEntity entity = new BookedTicketEntity();
        entity.setTicketId(orderItem.ticketId());
        entity.setOrderId(orderId);
        return entity;
    }

    private BigDecimal calculateTicketPrice(TicketEntity ticket) {
        if (ticket.getSeat() != null ) {
            Long seatClassId = ticket.getSeat().getSeatClassId();
            return switch (seatClassId.intValue()) {
                case 1 -> // Economy
                        new BigDecimal("1000000");
                case 2 -> // Business
                        new BigDecimal("3000000");
                case 3 -> // First Class
                        new BigDecimal("5000000");
                default -> new BigDecimal("1000000");
            };
        }
        return new BigDecimal("1000000");
    }
}
