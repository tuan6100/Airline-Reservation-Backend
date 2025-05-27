package vn.edu.hust.infrastructure.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.exception.SeatLockingException;
import vn.edu.hust.domain.model.aggregate.Seat;
import vn.edu.hust.domain.model.enumeration.SeatStatus;
import vn.edu.hust.domain.model.valueobj.FlightId;
import vn.edu.hust.domain.model.valueobj.SeatId;
import vn.edu.hust.domain.repository.SeatRepository;
import vn.edu.hust.infrastructure.entity.SeatEntity;
import vn.edu.hust.infrastructure.mapper.SeatMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class DomainSeatRepository implements SeatRepository {

    @Autowired private SeatJpaRepository seatJpaRepository;
    @Autowired private SeatMapper seatMapper;



    @Override
    public Seat findById(SeatId seatId) {
        return seatJpaRepository.findById(seatId.value())
                .map(seatMapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<Seat> findByFlightId(FlightId flightId) {
        return seatJpaRepository.findByFlightId(flightId.value())
                .stream()
                .map(seatMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Seat> findAvailableByFlightId(FlightId flightId) {
        return seatJpaRepository.findByFlightIdAndStatus(flightId.value(), SeatStatus.AVAILABLE.name())
                .stream()
                .map(seatMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Seat> findExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        return seatJpaRepository.findByStatusAndHoldUntilBefore(SeatStatus.ON_HOLD.name(), now)
                .stream()
                .map(seatMapper::toDomain)
                .collect(Collectors.toList());
    }


    @Override
    public void save(Seat seat) {
        SeatEntity entity = seatMapper.toEntity(seat);

        try {
            seatJpaRepository.save(entity);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.error(e.getMessage());
            throw new SeatLockingException("Xung đột khi lưu ghế có ID: " + seat.getSeatId().value() + ". Ghế đã bị thay đổi bởi giao dịch khác.");
        }
    }

    public boolean tryOptimisticLock(SeatId seatId, int expectedVersion) {
        try {
            SeatEntity entity = seatJpaRepository.findById(seatId.value())
                    .orElseThrow(() -> new SeatLockingException("Không tìm thấy ghế có ID: " + seatId.value()));
            return entity.getVersion() == expectedVersion;
        } catch (Exception e) {
            return false;
        }
    }
}
