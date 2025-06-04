package vn.edu.hust.infrastructure.mapper;

import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.aggregate.Seat;
import vn.edu.hust.infrastructure.entity.SeatEntity;

@Component
public class SeatMapper {

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
        seat.setVersion(entity.getVersion());
        return seat;
    }

    public SeatEntity toEntity(Seat domain) {
        if (domain == null) {
            return null;
        }
        SeatEntity entity = new SeatEntity();
        entity.setSeatId(domain.getSeatId());
        entity.setAircraftId(domain.getAircraftId());
        entity.setSeatCode(domain.getSeatCode());
        entity.setStatus(domain.getStatus());
        entity.setVersion(domain.getVersion());
        return entity;
    }
}