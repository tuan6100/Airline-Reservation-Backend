package vn.edu.hust.infrastructure.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.aggregate.Order;
import vn.edu.hust.domain.model.entity.OrderItem;
import vn.edu.hust.domain.model.valueobj.*;
import vn.edu.hust.infrastructure.entity.OrderEntity;
import vn.edu.hust.infrastructure.entity.OrderItemEntity;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class OrderMapper {

    @Autowired
    private OrderItemMapper orderItemMapper;

    public Order toDomain(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        Order order = Order.create(
                new CustomerId(entity.getCustomerId()),
                new BookingId(entity.getBookingId()),
                entity.getPromotionId() != null ? new PromotionId(entity.getPromotionId()) : null
        );
        order.setOrderId(new OrderId(entity.getId()));
        order.setStatus(entity.getStatus());
        order.setPaymentStatus(entity.getPaymentStatus());
        order.setTotalAmount(new Money(entity.getTotalAmount(), Currency.getInstance(entity.getCurrency())));
        order.setCreatedAt(entity.getCreatedAt());
        order.setUpdatedAt(entity.getUpdatedAt());
        order.setVersion(entity.getVersion());
        entity.getItems().forEach(itemEntity -> {
            OrderItem item = orderItemMapper.toDomain(itemEntity);
            order.addItem(item);
        });

        return order;
    }

    public OrderEntity toEntity(Order domain) {
        if (domain == null) {
            return null;
        }
        OrderEntity entity = new OrderEntity();
        if (domain.getOrderId() != null) {
            entity.setId(domain.getOrderId().value());
        }
        entity.setCustomerId(domain.getCustomerId().value());
        entity.setBookingId(domain.getBookingId().value());
        entity.setPromotionId(domain.getPromotionId() != null ? domain.getPromotionId().value() : null);
        entity.setStatus(domain.getStatus());
        entity.setPaymentStatus(domain.getPaymentStatus());
        entity.setTotalAmount(domain.getTotalAmount().getAmount());
        entity.setCurrency(domain.getTotalAmount().getCurrency().getCurrencyCode());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());
        List<OrderItemEntity> itemEntities = domain.getOrderItems().stream()
                .map(orderItemMapper::toEntity)
                .collect(Collectors.toList());

        entity.setItems(itemEntities);
        return entity;
    }
}