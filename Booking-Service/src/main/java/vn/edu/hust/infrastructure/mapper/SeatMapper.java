package vn.edu.hust.infrastructure.mapper;

import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.aggregate.Seat;
import vn.edu.hust.domain.model.enumeration.SeatStatus;
import vn.edu.hust.domain.model.valueobj.AircraftId;
import vn.edu.hust.domain.model.valueobj.FlightId;
import vn.edu.hust.domain.model.valueobj.SeatClassId;
import vn.edu.hust.domain.model.valueobj.SeatId;
import vn.edu.hust.infrastructure.entity.SeatEntity;

@Component
public class SeatMapper {
    public Seat toDomain(SeatEntity entity) {
        Seat seat = new Seat();
        seat.setSeatId(new SeatId(entity.getId()));
        seat.setFlightId(new FlightId(entity.getFlightId()));
        seat.setAircraftId(new AircraftId(entity.getAircraftId()));
        seat.setSeatClassId(new SeatClassId(entity.getSeatClassId()));
        seat.setStatus(SeatStatus.valueOf(entity.getStatus()));
        seat.setHoldUntil(entity.getHoldUntil());
        seat.setVersion(entity.getVersion());
        return seat;
    }

    public SeatEntity toEntity(Seat domain) {
        SeatEntity entity = new SeatEntity();
        entity.setId(domain.getSeatId().value());
        entity.setFlightId(domain.getFlightId().value());
        entity.setAircraftId(domain.getAircraftId().value());
        entity.setSeatClassId(domain.getSeatClassId().value());
        entity.setStatus(domain.getStatus().name());
        entity.setHoldUntil(domain.getHoldUntil());
        entity.setVersion(domain.getVersion());
        return entity;
    }
}