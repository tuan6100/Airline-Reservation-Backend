// TicketMapper.java
package vn.edu.hust.infrastructure.mapper;

import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.aggregate.Ticket;
import vn.edu.hust.domain.model.enumeration.TicketStatus;
import vn.edu.hust.domain.model.valueobj.*;
import vn.edu.hust.infrastructure.entity.TicketEntity;

@Component
public class TicketMapper {

    public Ticket toDomain(TicketEntity entity) {
        if (entity == null) {
            return null;
        }

        Ticket ticket = new Ticket();
        if (entity.getTicketId() != null) {
            ticket.setTicketId(new TicketId(entity.getTicketId()));
        }
        ticket.setTicketCode(entity.getTicketCode());
        ticket.setFlightId(new FlightId(entity.getFlightId()));
        ticket.setFlightDepartureTime(entity.getFlightDepartureTime());
        ticket.setSeatId(new SeatId(entity.getSeatId()));
        ticket.setCreatedAt(entity.getCreatedAt());
        ticket.setStatus(TicketStatus.fromValue(entity.getStatus()));

        // Map seat details if available
        if (entity.getSeat() != null) {
            var seatEntity = entity.getSeat();
            ticket.setSeatDetails(new SeatDetails(
                    new SeatId(seatEntity.getSeatId()),
                    new SeatClassId(seatEntity.getSeatClassId()),
                    new AircraftId(seatEntity.getAircraftId()),
                    seatEntity.getSeatCode(),
                    null // Price will be calculated separately
            ));
        }

        return ticket;
    }

    public TicketEntity toEntity(Ticket domain) {
        if (domain == null) {
            return null;
        }

        TicketEntity entity = new TicketEntity();
        if (domain.getTicketId() != null) {
            entity.setTicketId(domain.getTicketId().value());
        }
        entity.setTicketCode(domain.getTicketCode());
        entity.setFlightId(domain.getFlightId().value());
        entity.setFlightDepartureTime(domain.getFlightDepartureTime());
        entity.setSeatId(domain.getSeatId().value());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setStatus(domain.getStatus().getValue());

        return entity;
    }
}

// SeatMapper.java - Updated
