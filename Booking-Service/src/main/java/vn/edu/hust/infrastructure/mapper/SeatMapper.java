package vn.edu.hust.infrastructure.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.aggregate.Seat;
import vn.edu.hust.domain.model.valueobj.AircraftId;
import vn.edu.hust.domain.model.valueobj.SeatClassId;
import vn.edu.hust.domain.model.valueobj.SeatId;
import vn.edu.hust.infrastructure.entity.SeatEntity;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class SeatMapper {

    @Value("${domain.seat.hold-util}") private CharSequence setHoldUntil;

    public Seat toDomain(SeatEntity entity) {
        if (entity == null) {
            return null;
        }

        Seat seat = new Seat();
        seat.setSeatId(entity.getSeatId());
        seat.setSeatClassId(entity.getSeatClassId());
        seat.setAircraftId(entity.getAircraftId());
        seat.setSeatCode(entity.getSeatCode());
        seat.setStatus(entity.getStatus());
        seat.setHoldUntil(LocalDateTime.parse(setHoldUntil));
        seat.setVersion(entity.getVersion());
        return seat;
    }

    public SeatEntity toEntity(Seat domain) {
        if (domain == null) {
            return null;
        }
        SeatEntity entity = new SeatEntity();
        if (domain.getSeatId() != null) {
            entity.setSeatId(domain.getSeatId());
        }
        entity.setSeatClassId(domain.getSeatClassId());
        entity.setAircraftId(domain.getAircraftId());
        entity.setSeatCode(domain.getSeatCode());
        entity.setStatus(domain.getStatus());
        entity.setVersion(domain.getVersion());
        return entity;
    }

}