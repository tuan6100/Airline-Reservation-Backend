package vn.edu.hust.application.dto.command;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for creating a new order
 */
@Data
public class CreateOrderCommand {
    private Long bookingId;
    private Long customerId;
    private Long promotionId;
    private List<OrderItemCommand> items = new ArrayList<>();

    /**
     * Command for creating order items
     */
    @Data
    public static class OrderItemCommand {
        private Long ticketId;
        private Long flightId;
        private Long seatId;
        private Double price;
        private String currency;
        private String description;
    }
}