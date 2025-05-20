package vn.edu.hust.infrastructure.mapper;

import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.valueobj.*;
import vn.edu.hust.infrastructure.entity.SeatReservationEntity;

import java.util.Currency;

@Component
public class SeatReservationMapper {
    public SeatReservation toDomain(SeatReservationEntity entity) {
        return new SeatReservation(
                new SeatId(entity.getSeatId()),
                new FlightId(entity.getFlightId()),
                new SeatClass(entity.getSeatClassName()),
                new Money(entity.getPrice(), Currency.getInstance(entity.getCurrency()))
        );
    }

    public SeatReservationEntity toEntity(SeatReservation domain) {
        SeatReservationEntity entity = new SeatReservationEntity();
        entity.setSeatId(domain.seatId().value());
        entity.setFlightId(domain.flightId().value());
        entity.setSeatClassName(domain.seatClass().getName());
        entity.setPrice(domain.price().amount());
        entity.setCurrency(domain.price().currency().getCurrencyCode());
        return entity;
    }
}
