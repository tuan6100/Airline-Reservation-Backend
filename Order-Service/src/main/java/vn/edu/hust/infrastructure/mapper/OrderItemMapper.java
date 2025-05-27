package vn.edu.hust.infrastructure.mapper;

import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.entity.OrderItem;
import vn.edu.hust.domain.model.valueobj.*;
import vn.edu.hust.infrastructure.entity.OrderItemEntity;

import java.util.Currency;


@Component
public class OrderItemMapper {

    public OrderItem toDomain(OrderItemEntity entity) {
        if (entity == null) {
            return null;
        }

        OrderItem item = OrderItem.create(
                new TicketId(entity.getTicketId()),
                new FlightId(entity.getFlightId()),
                new SeatId(entity.getSeatId()),
                new Money(entity.getPrice(), Currency.getInstance(entity.getCurrency())),
                entity.getDescription()
        );
        if (entity.getId() != null) {
            try {
                java.lang.reflect.Field idField = OrderItem.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(item, entity.getId());
            } catch (Exception e) {
                System.err.println("Error setting ID field: " + e.getMessage());
            }
        }
        return item;
    }

    public OrderItemEntity toEntity(OrderItem domain) {
        if (domain == null) {
            return null;
        }
        OrderItemEntity entity = new OrderItemEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setTicketId(domain.getTicketId().value());
        entity.setFlightId(domain.getFlightId().value());
        entity.setSeatId(domain.getSeatId().value());
        entity.setPrice(domain.getPrice().getAmount());
        entity.setCurrency(domain.getPrice().getCurrency().getCurrencyCode());
        entity.setDescription(domain.getDescription());
        return entity;
    }
}