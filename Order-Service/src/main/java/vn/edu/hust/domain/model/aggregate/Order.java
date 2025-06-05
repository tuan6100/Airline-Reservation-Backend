package vn.edu.hust.domain.model.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import vn.edu.hust.application.dto.command.*;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.domain.model.enumeration.OrderStatus;
import vn.edu.hust.domain.model.enumeration.PaymentStatus;
import vn.edu.hust.domain.model.valueobj.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Aggregate
@Getter
@NoArgsConstructor
public class Order {
    @AggregateIdentifier
    private Long orderId;
    private Long customerId;
    private String bookingId;
    private Long promotionId;
    private List<OrderItem> orderItems = new ArrayList<>();
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @CommandHandler
    public Order(CreateOrderCommand command) {
        AggregateLifecycle.apply(new OrderCreatedEvent(
                command.getOrderId(),
                command.getCustomerId(),
                command.getBookingId().toString(),
                command.getPromotionId(),
                OrderStatus.PENDING,
                BigDecimal.ZERO,
                "VND",
                LocalDateTime.now()
        ));
        if (command.getItems() != null) {
            command.getItems().forEach(item -> {
                AggregateLifecycle.apply(new OrderItemAddedEvent(
                        command.getOrderId(),
                        item.getTicketId(),
                        item.getFlightId(),
                        item.getSeatId(),
                        BigDecimal.valueOf(item.getPrice()),
                        item.getCurrency(),
                        item.getDescription()
                ));
            });
        }
    }

    @CommandHandler
    public void handle(AddOrderItemCommand command) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot add items to non-pending order");
        }
        boolean itemExists = orderItems.stream()
                .anyMatch(item -> item.ticketId().equals(command.getTicketId()));
        if (itemExists) {
            throw new IllegalArgumentException("Order already contains item with ticket ID: " + command.getTicketId());
        }

        AggregateLifecycle.apply(new OrderItemAddedEvent(
                orderId,
                command.getTicketId(),
                command.getFlightId(),
                command.getSeatId(),
                BigDecimal.valueOf(command.getPrice()),
                command.getCurrency(),
                command.getDescription()
        ));
    }

    @CommandHandler
    public void handle(ConfirmOrderCommand command) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order must be in PENDING state to be confirmed");
        }
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("Cannot confirm an empty order");
        }
        AggregateLifecycle.apply(new OrderConfirmedEvent(
                orderId,
                bookingId,
                totalAmount,
                currency
        ));
    }

    @CommandHandler
    public void handle(CancelOrderCommand command) {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        if (status == OrderStatus.PAID) {
            throw new IllegalStateException("Paid order cannot be cancelled directly, must be refunded");
        }
        AggregateLifecycle.apply(new OrderCancelledEvent(
                orderId,
                bookingId,
                command.getReason(),
                LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(MarkOrderPaidCommand command) {
        if (status != OrderStatus.PAYMENT_PENDING && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be confirmed or payment pending to mark as paid");
        }

        AggregateLifecycle.apply(new OrderPaidEvent(
                orderId,
                command.getPaymentId(),
                totalAmount,
                currency,
                LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(MarkOrderRefundedCommand command) {
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("Only paid orders can be refunded");
        }

        AggregateLifecycle.apply(new OrderRefundedEvent(
                orderId,
                totalAmount,
                currency,
                LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(MarkOrderPaymentPendingCommand command) {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be confirmed before marking payment pending");
        }

        AggregateLifecycle.apply(new OrderPaymentPendingEvent(
                orderId,
                LocalDateTime.now()
        ));
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent event) {
        this.orderId = event.orderId();
        this.customerId = event.customerId();
        this.bookingId = event.bookingId();
        this.promotionId = event.promotionId();
        this.status = event.status();
        this.paymentStatus = PaymentStatus.NOT_PAID;
        this.totalAmount = event.totalAmount();
        this.currency = event.currency();
        this.createdAt = event.createdAt();
        this.updatedAt = event.createdAt();
    }

    @EventSourcingHandler
    public void on(OrderItemAddedEvent event) {
        OrderItem item = new OrderItem(
                event.ticketId(),
                event.flightId(),
                event.seatId(),
                event.price(),
                event.currency(),
                event.description()
        );
        this.orderItems.add(item);
        recalculateTotalAmount();
        this.updatedAt = LocalDateTime.now();
    }

    @EventSourcingHandler
    public void on(OrderConfirmedEvent event) {
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    @EventSourcingHandler
    public void on(OrderCancelledEvent event) {
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = event.cancelledAt();
    }

    @EventSourcingHandler
    public void on(OrderPaidEvent event) {
        this.status = OrderStatus.PAID;
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.updatedAt = event.paidAt();
    }

    private void recalculateTotalAmount() {
        BigDecimal newTotal = orderItems.stream()
                .map(OrderItem::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (promotionId != null) {
            newTotal = newTotal.multiply(BigDecimal.valueOf(0.9));
        }

        this.totalAmount = newTotal;
    }
}
