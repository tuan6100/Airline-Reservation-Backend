package vn.edu.hust.application.eventhandler;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.domain.exception.OrderNotFoundException;
import vn.edu.hust.domain.model.enumeration.OrderStatus;
import vn.edu.hust.domain.model.enumeration.PaymentStatus;
import vn.edu.hust.infrastructure.entity.OrderEntity;
import vn.edu.hust.infrastructure.entity.OrderItemEntity;
import vn.edu.hust.infrastructure.repository.OrderJpaRepository;

import java.time.LocalDateTime;

@Component
public class OrderEventHandler {

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @EventHandler
    public void on(OrderCreatedEvent event) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setCustomerId(event.customerId());
        orderEntity.setBookingId(event.bookingId());
        orderEntity.setOrderStatus(event.status());
        orderEntity.setPaymentStatus(PaymentStatus.NOT_PAID);
        orderEntity.setTotalPrice(event.totalPrice());
        orderEntity.setCreatedAt(event.createdAt());
        orderEntity.setUpdatedAt(event.createdAt());
        orderJpaRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderItemAddedEvent event) throws OrderNotFoundException {
        OrderEntity orderEntity = orderJpaRepository.findByBookingId(event.bookingId())
                .orElseThrow(() -> new OrderNotFoundException("Order is not created "));
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderEntity(orderEntity);
        orderItemEntity.setAdded_at(LocalDateTime.now());
        orderEntity.getOrderItemEntities().add(orderItemEntity);
        orderJpaRepository.save(orderEntity);
        orderEntity.setTotalPrice(orderEntity.getTotalPrice() + event.item().getPrice());
        orderJpaRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderConfirmedEvent event) {
        OrderEntity orderEntity = orderJpaRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException("Order is not created "));
        orderEntity.setOrderStatus(OrderStatus.CONFIRMED);
        orderEntity.setUpdatedAt(java.time.LocalDateTime.now());
        orderJpaRepository.save(orderEntity);
    }

    @EventHandler
    public void on(PromotionAppliedEvent event) {
        OrderEntity orderEntity = orderJpaRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException("Order is not created "));
        Long price = orderEntity.getTotalPrice();
        Long discount = event.promotion().discount();
        switch (event.promotion().discountType()) {
            case VOUCHER -> price = (price < discount)? 0 : price - discount;
            case COUPON -> price = price * discount / 100;
        }
        orderEntity.setTotalPrice(price);
        orderJpaRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderCancelledEvent event) {
        OrderEntity orderEntity = orderJpaRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException("Order is not created "));
        orderEntity.setOrderStatus(OrderStatus.CANCELLED);
        orderEntity.setUpdatedAt(event.cancelledAt());
        orderJpaRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderPaidEvent event) {
        OrderEntity orderEntity = orderJpaRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException("Order is not created "));
        orderEntity.setOrderStatus(OrderStatus.PAID);
        orderEntity.setPaymentStatus(PaymentStatus.COMPLETED_PAYMENT);
        orderEntity.setUpdatedAt(event.paidAt());
        orderJpaRepository.save(orderEntity);
    }
}
