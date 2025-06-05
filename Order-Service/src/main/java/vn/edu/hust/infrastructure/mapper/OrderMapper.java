package vn.edu.hust.infrastructure.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.aggregate.Order;
import vn.edu.hust.domain.model.enumeration.OrderStatus;
import vn.edu.hust.domain.model.enumeration.PaymentStatus;
import vn.edu.hust.domain.model.valueobj.*;
import vn.edu.hust.infrastructure.entity.BookedTicketEntity;
import vn.edu.hust.infrastructure.entity.OrderEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Component
public class OrderMapper {

    @Autowired
    private BookedTicketMapper bookedTicketMapper;

    public Order toDomain(OrderEntity entity) {
        if (entity == null) {
            return null;
        }
        Order order = createOrderInstance();
        setField(order, "orderId", entity.getOrderId());
        setField(order, "customerId", entity.getCustomerId());
        setField(order, "bookingId", entity.getBookingId());
        setField(order, "promotionId", entity.getPromotionId());
        OrderStatus status = entity.getStatus() != null ?
                OrderStatus.valueOf(entity.getStatus()) : OrderStatus.PENDING;
        setField(order, "status", status);
        PaymentStatus paymentStatus = entity.getPaymentStatus() != null ?
                PaymentStatus.valueOf(entity.getPaymentStatus()) : PaymentStatus.NOT_PAID;
        setField(order, "paymentStatus", paymentStatus);
        BigDecimal amount = entity.getTotalAmount() != null ? entity.getTotalAmount() : BigDecimal.ZERO;
        String currencyCode = entity.getCurrency() != null ? entity.getCurrency() : "VND";
        setField(order, "totalAmount", amount);
        setField(order, "currency", currencyCode);
        setField(order, "createdAt", entity.getCreatedAt());
        setField(order, "updatedAt", entity.getUpdatedAt());
        if (entity.getBookedTickets() != null) {
            List<OrderItem> orderItems = entity.getBookedTickets().stream()
                    .map(bookedTicketMapper::toOrderItem)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            setField(order, "orderItems", orderItems);
        }

        return order;
    }

    public OrderEntity toEntity(Order domain) {
        if (domain == null) {
            return null;
        }
        OrderEntity entity = new OrderEntity();
        if (domain.getOrderId() != null) {
            entity.setOrderId(domain.getOrderId());
        }
        entity.setCustomerId(domain.getCustomerId());
        entity.setBookingId(domain.getBookingId());
        entity.setPromotionId(domain.getPromotionId());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : OrderStatus.PENDING.name());
        entity.setPaymentStatus(domain.getPaymentStatus() != null ?
                domain.getPaymentStatus().name() : PaymentStatus.NOT_PAID.name());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setCurrency(domain.getCurrency());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(0);
        if (domain.getOrderItems() != null) {
            List<BookedTicketEntity> bookedTickets = domain.getOrderItems().stream()
                    .map(item -> bookedTicketMapper.toBookedTicketEntity(item, domain.getOrderId()))
                    .collect(Collectors.toList());
            entity.setBookedTickets(bookedTickets);
        }
        return entity;
    }

    private Order createOrderInstance() {
        try {
            return Order.class.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Order instance", e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            OrderMapper.log.warn("Could not set field {}: {}", fieldName, e.getMessage());
        }
    }
}
