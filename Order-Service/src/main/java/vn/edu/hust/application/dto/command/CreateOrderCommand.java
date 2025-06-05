package vn.edu.hust.application.dto.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateOrderCommand {
    @TargetAggregateIdentifier
    private Long orderId;
    private Long customerId;
    private String bookingId;
    private Long promotionId;
    private List<OrderItemRequest> items;

    @Data
    @NoArgsConstructor
    public static class OrderItemRequest {
        private Long ticketId;
        private Long flightId;
        private Long seatId;
        private Double price;
        private String currency;
        private String description;
    }
}