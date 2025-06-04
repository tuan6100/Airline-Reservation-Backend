package vn.edu.hust.infrastructure.mapper;

import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.aggregate.Ticket;
import vn.edu.hust.domain.model.enumeration.TicketStatus;
import vn.edu.hust.infrastructure.entity.TicketEntity;

@Component
public class TicketMapper {

    public Ticket toDomain(TicketEntity entity) {
        if (entity == null) {
            return null;
        }
        Ticket ticket = new Ticket();
        setField(ticket, "ticketId", entity.getTicketId());
        setField(ticket, "ticketCode", entity.getTicketCode().toString());
        setField(ticket, "flightId", entity.getFlightId());
        setField(ticket, "flightDepartureTime", entity.getFlightDepartureTime());
        setField(ticket, "seatId", entity.getSeatId());
        setField(ticket, "status", TicketStatus.fromValue(entity.getStatus()));
        setField(ticket, "createdAt", entity.getCreatedAt());

        return ticket;
    }

    public TicketEntity toEntity(Ticket domain) {
        if (domain == null) {
            return null;
        }

        TicketEntity entity = new TicketEntity();
        entity.setTicketId(domain.getTicketId());
        entity.setTicketCode(java.util.UUID.fromString(domain.getTicketCode()));
        entity.setFlightId(domain.getFlightId());
        entity.setFlightDepartureTime(domain.getFlightDepartureTime());
        entity.setSeatId(domain.getSeatId());
        entity.setStatus(domain.getStatus().getValue());
        entity.setCreatedAt(domain.getCreatedAt());

        return entity;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            // Handle exception appropriately
        }
    }
}
