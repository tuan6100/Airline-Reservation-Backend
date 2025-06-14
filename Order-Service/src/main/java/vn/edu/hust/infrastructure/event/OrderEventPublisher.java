package vn.edu.hust.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.infrastructure.dto.InitiatePaymentRequest;
import vn.edu.hust.infrastructure.dto.ResetTimeoutRequest;
import vn.edu.hust.infrastructure.entity.OrderEntity;
import vn.edu.hust.infrastructure.outbox.OutboxHelper;
import vn.edu.hust.infrastructure.repository.OrderJpaRepository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Slf4j
@Component
public class OrderEventPublisher {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OutboxHelper outboxHelper;

    @Autowired
    private OrderJpaRepository orderJpaRepository;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            Optional<OrderEntity> optional = orderJpaRepository.findByBookingId(event.bookingId());
            optional.ifPresent(entity ->
                    outboxHelper.saveToOutbox(entity.getOrderId(), "OrderCreatedEvent", event));
        } catch (Exception e) {
            OrderEventPublisher.log.error("Failed to save OrderCreatedEvent to outbox: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            retryFor = {ExecutionException.class, InterruptedException.class},
            backoff = @Backoff(delay = 500)
    )
    public void handleItemRemovedEvent(ItemRemovedEvent event) throws ExecutionException, InterruptedException {
        kafkaTemplate.send("item-removed.request.payment-group.topic", event).get();
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        try {
            CompletableFuture<Void> paymentFuture = CompletableFuture.runAsync(() -> {
                try {
                    sendPaymentInitiationRequest(event);
                } catch (ExecutionException | InterruptedException e) {
                    OrderEventPublisher.log.error("Failed to send payment initiation request", e);
                }
            });
            CompletableFuture<Void> timeoutFuture = CompletableFuture.runAsync(() -> {
                try {
                    sendClearTimeoutRequest(event.orderId());
                } catch (ExecutionException | InterruptedException e) {
                    OrderEventPublisher.log.error("Failed to send reset timeout request", e);
                }
            });
            CompletableFuture.allOf(paymentFuture, timeoutFuture).join();
        } catch (Exception e) {
            OrderEventPublisher.log.error("Failed to process order confirmed event", e);
        }
    }

    @Retryable(
            retryFor = {ExecutionException.class, InterruptedException.class},
            backoff = @Backoff(delay = 500)
    )
    private void sendPaymentInitiationRequest(OrderConfirmedEvent event) throws ExecutionException, InterruptedException {
        Optional<Long> optional = orderJpaRepository.findCustomerIdByOrderId(event.orderId());
        if (optional.isPresent()) {
            Long customerId = optional.get();
            InitiatePaymentRequest request = new InitiatePaymentRequest(
                    event.orderId(),
                    customerId,
                    event.totalPrice(),
                    event.currency()
            );
            kafkaTemplate.send("order-confirmed.payment-group.topic", request).get();
            OrderEventPublisher.log.info("Payment has initiated for order {}", request.orderId());
        }
    }

    @Retryable(
            retryFor = {ExecutionException.class, InterruptedException.class},
            backoff = @Backoff(delay = 500)
    )
    private void sendClearTimeoutRequest(Long orderId) throws ExecutionException, InterruptedException {
        Optional<ResetTimeoutRequest> optional = orderJpaRepository.findSeatAndTicketByOrderId(orderId);
        if (optional.isPresent()) {
            ResetTimeoutRequest request = optional.get();
            kafkaTemplate.send("order-confirmed.booking-group.topic",request).get();
            OrderEventPublisher.log.info("Hold timeout has reset for set {} and ticket {}", request.seatId(), request.ticketId());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        try {
            sendBookingReleasedEvent(event.bookingId());
            outboxHelper.saveToOutbox(event.orderId(), "OrderCancelledEvent", event);
        } catch (Exception e) {
            OrderEventPublisher.log.error("Failed to save OrderCancelledEvent to outbox: {}", e.getMessage());
        }
    }

    @Retryable(
            retryFor = {ExecutionException.class, InterruptedException.class},
            backoff = @Backoff(delay = 500)
    )
    private void sendBookingReleasedEvent(String bookingId) throws ExecutionException, InterruptedException {
        kafkaTemplate.send("order-cancelled.booking-group.topic", bookingId).get();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        try {
            outboxHelper.saveToOutbox(event.orderId(), "OrderStatusChangedEvent", event);
        } catch (Exception e) {
            OrderEventPublisher.log.error("Failed to save OrderStatusChangedEvent to outbox: {}", e.getMessage());
        }
    }
}