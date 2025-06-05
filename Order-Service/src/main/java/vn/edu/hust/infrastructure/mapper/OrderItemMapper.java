package vn.edu.hust.infrastructure.mapper;

import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.valueobj.*;
import vn.edu.hust.infrastructure.entity.OrderItemEntity;

@Component
public class OrderItemMapper {

    public OrderItem toDomain(OrderItemEntity entity) {
        if (entity == null) {
            return null;
        }

        return OrderItem.withId(
                entity.getId(),
                entity.getTicketId(),
                entity.getFlightId(),
                entity.getSeatId(),
                entity.getPrice(),
                entity.getCurrency(),
                entity.getDescription()
        );
    }

    public OrderItemEntity toEntity(OrderItem domain) {
        if (domain == null) {
            return null;
        }

        OrderItemEntity entity = new OrderItemEntity();
        if (domain.id() != null) {
            entity.setId(domain.id());
        }
        entity.setTicketId(domain.ticketId());
        entity.setFlightId(domain.flightId());
        entity.setSeatId(domain.seatId());
        entity.setPrice(domain.price());
        entity.setCurrency(domain.currency());
        entity.setDescription(domain.description());

        return entity;
    }
}