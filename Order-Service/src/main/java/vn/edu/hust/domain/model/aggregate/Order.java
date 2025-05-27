package vn.edu.hust.domain.model.aggregate;

import lombok.Data;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import vn.edu.hust.infrastructure.event.DomainEventPublisher;
import vn.edu.hust.domain.event.OrderCancelledEvent;
import vn.edu.hust.domain.event.OrderConfirmedEvent;
import vn.edu.hust.domain.event.OrderCreatedEvent;
import vn.edu.hust.domain.event.OrderStatusChangedEvent;
import vn.edu.hust.domain.exception.OrderAlreadyConfirmedException;
import vn.edu.hust.domain.model.entity.OrderItem;
import vn.edu.hust.domain.model.enumeration.OrderStatus;
import vn.edu.hust.domain.model.enumeration.PaymentStatus;
import vn.edu.hust.domain.model.valueobj.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Aggregate
@Data
public class Order {
    @AggregateIdentifier
    private OrderId orderId;
    private CustomerId customerId;
    private BookingId bookingId;
    private PromotionId promotionId;
    private List<OrderItem> orderItems = new ArrayList<>();
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private Money totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int version;

    // Private constructor for creation via factory method
    private Order() {
    }

    /**
     * Factory method to create a new order
     */
    public static Order create(CustomerId customerId, BookingId bookingId, PromotionId promotionId) {
        Order order = new Order();
        order.customerId = customerId;
        order.bookingId = bookingId;
        order.promotionId = promotionId;
        order.status = OrderStatus.PENDING;
        order.paymentStatus = PaymentStatus.NOT_PAID;
        order.totalAmount = Money.ZERO;
        order.createdAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();
        order.version = 0;

        return order;
    }

    /**
     * Method to be called after the order is persisted and has an ID
     */
    public void afterCreate() {
        if (this.orderId != null) {
            // Raise domain event
            DomainEventPublisher.instance().publish(new OrderCreatedEvent(
                    this.orderId,
                    this.customerId,
                    this.bookingId,
                    this.promotionId,
                    this.status,
                    this.totalAmount,
                    this.createdAt
            ));
        }
    }


    public void addItem(OrderItem item) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify a non-pending order");
        }
        boolean itemExists = orderItems.stream()
                .anyMatch(i -> i.getTicketId().equals(item.getTicketId()));

        if (itemExists) {
            throw new IllegalArgumentException("Order already contains item with ticket ID: " + item.getTicketId());
        }
        orderItems.add(item);
        recalculateTotalAmount();
        this.updatedAt = LocalDateTime.now();
    }


    private void recalculateTotalAmount() {
        Money newTotal = Money.ZERO;

        for (OrderItem item : orderItems) {
            newTotal = newTotal.add(item.getPrice());
        }
        if (promotionId != null) {
            newTotal = newTotal.multiply(0.9);
        }

        this.totalAmount = newTotal;
    }


    public void confirm() {
        if (status == OrderStatus.CONFIRMED) {
            throw new OrderAlreadyConfirmedException("Order is already confirmed: " + orderId);
        }

        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm a cancelled order: " + orderId);
        }

        // Order must have items
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("Cannot confirm an empty order");
        }

        OrderStatus oldStatus = this.status;
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
        this.version++;

        // Raise domain event
        DomainEventPublisher.instance().publish(new OrderConfirmedEvent(
                this.orderId,
                this.bookingId,
                this.totalAmount
        ));

        DomainEventPublisher.instance().publish(new OrderStatusChangedEvent(
                this.orderId,
                oldStatus,
                this.status,
                this.updatedAt
        ));
    }


    public void markPaymentPending() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be confirmed before payment: " + orderId);
        }

        if (paymentStatus != PaymentStatus.NOT_PAID) {
            throw new IllegalStateException("Order payment already initiated: " + orderId);
        }

        this.paymentStatus = PaymentStatus.PENDING;
        this.status = OrderStatus.PAYMENT_PENDING;
        this.updatedAt = LocalDateTime.now();
        this.version++;

        DomainEventPublisher.instance().publish(new OrderStatusChangedEvent(
                this.orderId,
                OrderStatus.CONFIRMED,
                this.status,
                this.updatedAt
        ));
    }

    public void markPaid() {
        if (status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Order must be in payment pending state: " + orderId);
        }
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.status = OrderStatus.PAID;
        this.updatedAt = LocalDateTime.now();
        this.version++;
        DomainEventPublisher.instance().publish(new OrderStatusChangedEvent(
                this.orderId,
                OrderStatus.PAYMENT_PENDING,
                this.status,
                this.updatedAt
        ));
    }

    public void cancel(String reason) {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled: " + orderId);
        }
        if (status == OrderStatus.PAID) {
            throw new IllegalStateException("Paid order cannot be cancelled directly, must be refunded: " + orderId);
        }
        OrderStatus oldStatus = this.status;
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        this.version++;
        DomainEventPublisher.instance().publish(new OrderCancelledEvent(
                this.orderId,
                this.bookingId,
                reason,
                this.updatedAt
        ));
        DomainEventPublisher.instance().publish(new OrderStatusChangedEvent(
                this.orderId,
                oldStatus,
                this.status,
                this.updatedAt
        ));
    }


    public void markRefunded() {
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("Only paid orders can be refunded: " + orderId);
        }
        this.status = OrderStatus.REFUNDED;
        this.paymentStatus = PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
        this.version++;
        DomainEventPublisher.instance().publish(new OrderStatusChangedEvent(
                this.orderId,
                OrderStatus.PAID,
                this.status,
                this.updatedAt
        ));
    }

    private void registerEvent(Object event) {
        DomainEventPublisher.instance().publish(event);
    }
}
