package vn.edu.hust.infrastructure.mapper;

import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.aggregate.Seat;
import vn.edu.hust.domain.model.valueobj.AircraftId;
import vn.edu.hust.domain.model.valueobj.SeatClassId;
import vn.edu.hust.domain.model.valueobj.SeatId;
import vn.edu.hust.infrastructure.entity.SeatEntity;

@Component
public class SeatMapper {

    public Seat toDomain(SeatEntity entity) {
        if (entity == null) {
            return null;
        }

        Seat seat = new Seat();
        seat.setSeatId(new SeatId(entity.getSeatId()));
        seat.setSeatClassId(new SeatClassId(entity.getSeatClassId()));
        seat.setAircraftId(new AircraftId(entity.getAircraftId()));
        seat.setSeatCode(entity.getSeatCode());
        seat.setIsAvailable(entity.getIsAvailable());
        seat.setHoldUntil(entity.getHoldUntil());
        seat.setVersion(entity.getVersion());

        return seat;
    }

    public SeatEntity toEntity(Seat domain) {
        if (domain == null) {
            return null;
        }

        SeatEntity entity = new SeatEntity();
        if (domain.getSeatId() != null) {
            entity.setSeatId(domain.getSeatId().value());
        }
        entity.setSeatClassId(domain.getSeatClassId().value());
        entity.setAircraftId(domain.getAircraftId().value());
        entity.setSeatCode(domain.getSeatCode());
        entity.setIsAvailable(domain.getIsAvailable());
        entity.setHoldUntil(domain.getHoldUntil());
        entity.setVersion(domain.getVersion());

        return entity;
    }
}