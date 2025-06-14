package vn.edu.hust.domain.model.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import vn.edu.hust.application.dto.command.*;
import vn.edu.hust.application.dto.query.TicketBookedDTO;
import vn.edu.hust.application.enumeration.CurrencyUnit;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.domain.model.enumeration.DiscountType;
import vn.edu.hust.domain.model.enumeration.OrderStatus;
import vn.edu.hust.domain.model.enumeration.PaymentStatus;
import vn.edu.hust.domain.model.valueobj.OrderItem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


@Aggregate
@Getter
@Setter
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
    private Long totalPrice;
    private CurrencyUnit currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final AtomicBoolean isFullyInitialized = new AtomicBoolean(false);

    @CommandHandler
    public Order(CreateOrderCommand command) {
        if (command.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (command.getBookingId() == null || command.getBookingId().trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be null or empty");
        }
        AggregateLifecycle.apply(new OrderCreatedEvent(
                command.getCustomerId(),
                command.getBookingId(),
                OrderStatus.PENDING,
                0L,
                CurrencyUnit.getCurrencyUnitByNation(command.getNation()),
                LocalDateTime.now()
        ));
        if (command.getItem() != null) {
            AggregateLifecycle.apply(new ItemAddedEvent(
                command.getBookingId(), command.getItem()
            ));
        }
    }

    @CommandHandler
    public void handle(AddOrderItemCommand command) {
        if (!isFullyInitialized.get()) {
            throw new IllegalStateException("Order is not fully initialized yet. Please wait.");
        }
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot add items to non-pending order");
        }
        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new IllegalArgumentException("No items provided to add to order");
        }
        for (TicketBookedDTO item : command.getItems()) {
            AggregateLifecycle.apply(new ItemAddedEvent(
                    this.bookingId,
                    item
            ));
        }
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
                totalPrice,
                currency
        ));
    }

    @CommandHandler
    public void handle(ApplyPromotionToOrderCommand command) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order must be in PENDING state to be confirmed");
        }
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("Cannot confirm an empty order");
        }
        AggregateLifecycle.apply(new PromotionAppliedEvent(
                orderId,
                command.getPromotion()
        ));
    }

    @CommandHandler
    public void handle(RemoveItemFromOrderCommand command) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order must be in PENDING state to be confirmed");
        }
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("Cannot remove item from an empty order");
        }
        AggregateLifecycle.apply((new ItemRemovedEvent(
                orderId,
                bookingId,
                command.getTicketId()
        )));
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
                totalPrice,
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
                totalPrice,
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
        this.customerId = event.customerId();
        this.bookingId = event.bookingId();
        this.status = event.status();
        this.paymentStatus = PaymentStatus.NOT_PAID;
        this.totalPrice = event.totalPrice();
        this.currency = event.currency();
        this.createdAt = event.createdAt();
        this.updatedAt = event.createdAt();
        this.isFullyInitialized.set(true);
    }

    @EventSourcingHandler
    public void on(ItemAddedEvent event) {
        OrderItem item = new OrderItem(
                event.item().getTicketId(),
                event.item().getPrice()
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
    public void on(PromotionAppliedEvent event) {
        this.promotionId = event.promotion().promotionId();
        recalculateTotalAmount(event.promotion().discount(), event.promotion().discountType());
    }

    @EventSourcingHandler
    public void on(ItemRemovedEvent event) {
        this.orderItems.removeIf(item -> item.ticketId().equals(event.ticketId()));
        recalculateTotalAmount();
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
        this.paymentStatus = PaymentStatus.COMPLETED_PAYMENT;
        this.updatedAt = event.paidAt();
    }

    private void recalculateTotalAmount() {
        this.totalPrice = this.orderItems.stream()
                .mapToLong(OrderItem::price)
                .sum();
    }

    private void recalculateTotalAmount(Long discount, DiscountType discountType) {
        switch (discountType) {
            case COUPON -> {
                if (discount != 0) {
                    discount /= 100;
                    this.totalPrice *= discount;
                }
            }
            case VOUCHER -> this.totalPrice = this.totalPrice < discount ? 0 : this.totalPrice - discount;
        }

    }
}
