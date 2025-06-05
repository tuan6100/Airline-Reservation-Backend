package vn.edu.hust.application.eventhandler;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.infrastructure.entity.OrderEntity;
import vn.edu.hust.infrastructure.entity.OrderItemEntity;
import vn.edu.hust.infrastructure.repository.OrderItemJpaRepository;
import vn.edu.hust.infrastructure.repository.OrderJpaRepository;

@Component
public class OrderEventHandler {

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    @EventHandler
    public void on(OrderCreatedEvent event) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(event.orderId());
        orderEntity.setCustomerId(event.customerId());
        orderEntity.setBookingId(event.bookingId());
        orderEntity.setPromotionId(event.promotionId());
        orderEntity.setStatus(event.status().name());
        orderEntity.setPaymentStatus("NOT_PAID");
        orderEntity.setTotalAmount(event.totalAmount());
        orderEntity.setCurrency(event.currency());
        orderEntity.setCreatedAt(event.createdAt());
        orderEntity.setUpdatedAt(event.createdAt());

        orderJpaRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderItemAddedEvent event) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderId(event.orderId());
        itemEntity.setTicketId(event.ticketId());
        itemEntity.setFlightId(event.flightId());
        itemEntity.setSeatId(event.seatId());
        itemEntity.setPrice(event.price());
        itemEntity.setCurrency(event.currency());
        itemEntity.setDescription(event.description());

        orderItemJpaRepository.save(itemEntity);

        OrderEntity orderEntity = orderJpaRepository.findById(event.orderId()).get();
        var items = orderItemJpaRepository.findByOrderId(event.orderId());
        var totalAmount = items.stream()
                .map(OrderItemEntity::getPrice)
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
