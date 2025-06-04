package vn.edu.hust.infrastructure.mapper;

import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.aggregate.Ticket;
import vn.edu.hust.infrastructure.entity.TicketEntity;

@Component
public class TicketMapper {

    public Ticket toDomain(TicketEntity entity) {
        if (entity == null) {
            return null;
        }
        return null;
    }

    public TicketEntity toEntity(Ticket domain) {
        if (domain == null) {
            return null;
        }
        TicketEntity entity = new TicketEntity();
        entity.setTicketId(domain.getTicketId());
        entity.setTicketCode(domain.getTicketCode());
        entity.setFlightId(domain.getFlightId());
        entity.setFlightDepartureTime(domain.getFlightDepartureTime());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        if (domain.getStatus() == vn.edu.hust.domain.model.enumeration.TicketStatus.BOOKED) {
            entity.setBookingId(domain.getBookingId());
        }
        return entity;
    }
}