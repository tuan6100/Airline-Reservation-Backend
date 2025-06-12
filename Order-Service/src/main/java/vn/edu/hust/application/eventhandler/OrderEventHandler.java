package vn.edu.hust.application.eventhandler;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.domain.model.enumeration.PaymentStatus;
import vn.edu.hust.infrastructure.entity.OrderEntity;
import vn.edu.hust.infrastructure.repository.OrderJpaRepository;

@Component
public class OrderEventHandler {

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @EventHandler
    public void on(OrderCreatedEvent event) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setCustomerId(event.customerId());
        orderEntity.setBookingId(event.bookingId());
        orderEntity.setStatus(event.status().name());
        orderEntity.setPaymentStatus(PaymentStatus.NOT_PAID);
        orderEntity.setTotalAmount(event.totalAmount());
        orderEntity.setCreatedAt(event.createdAt());
        orderEntity.setUpdatedAt(event.createdAt());
        orderJpaRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderItemAddedEvent event) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(event.orderId());
        orderEntity.setBookingId(event.bookingId());
        orderEntity.
        orderJpaRepository.save(orderEntity);
        var orders = orderJpaRepository.findByOrderId(event.orderId());
        var totalAmount = orders.stream()
                .map(OrderEntity::getPrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        if (orderEntity.getPromotionId() != null) {
            totalAmount = totalAmount.multiply(java.math.BigDecimal.valueOf(0.9));
        }
        orderEntity.setTotalAmount(totalAmount);
        orderJpaRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderConfirmedEvent event) {
        OrderEntity orderEntity = orderJpaRepository.findById(event.orderId()).get();
        orderEntity.setStatus("CONFIRMED");
        orderEntity.setUpdatedAt(java.time.LocalDateTime.now());
        orderJpaRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderCancelledEvent event) {
        OrderEntity orderEntity = orderJpaRepository.findById(event.orderId()).get();
        orderEntity.setStatus("CANCELLED");
        orderEntity.setUpdatedAt(event.cancelledAt());
        orderJpaRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderPaidEvent event) {
        OrderEntity orderEntity = orderJpaRepository.findById(event.orderId()).get();
        orderEntity.setStatus("PAID");
        orderEntity.setPaymentStatus("COMPLETED");
        orderEntity.setUpdatedAt(event.paidAt());
        orderJpaRepository.save(orderEntity);
    }
}
