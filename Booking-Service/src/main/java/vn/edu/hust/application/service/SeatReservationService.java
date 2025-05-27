package vn.edu.hust.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.domain.exception.SeatLockingException;
import vn.edu.hust.domain.exception.SeatNotAvailableException;
import vn.edu.hust.domain.model.aggregate.Seat;
import vn.edu.hust.domain.model.enumeration.SeatStatus;
import vn.edu.hust.domain.model.valueobj.CustomerId;
import vn.edu.hust.domain.model.valueobj.FlightId;
import vn.edu.hust.domain.model.valueobj.SeatId;
import vn.edu.hust.domain.repository.SeatRepository;
import vn.edu.hust.infrastructure.service.OptimisticLockingService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý đặt chỗ với cơ chế khóa lạc quan (optimistic locking)
 */
@Slf4j
@Service
public class SeatReservationService {

    @Autowired private SeatRepository seatRepository;
    @Autowired private OptimisticLockingService lockingService;
    private static final int MAX_RETRIES = 3;
    private static final long DEFAULT_HOLD_DURATION_MINUTES = 15;

    @Transactional(readOnly = true)
    public List<Seat> getAvailableSeats(FlightId flightId) {
        return seatRepository.findAvailableByFlightId(flightId);
    }

    @Transactional
    public Seat holdSeat(SeatId seatId, CustomerId customerId) {
        return holdSeat(seatId, customerId, Duration.ofMinutes(DEFAULT_HOLD_DURATION_MINUTES));
    }

    @Transactional
    public Seat holdSeat(SeatId seatId, CustomerId customerId, Duration holdDuration) {
        return lockingService.executeWithOptimisticLock(seatId, seat -> {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                if (seat.isHoldExpired()) {
                    seat.release();
                } else {
                    throw new SeatNotAvailableException("Ghế không có sẵn: " + seatId.value());
                }
            }
            seat.hold(customerId, holdDuration);
        });
    }

    @Transactional
    public Seat reserveSeat(SeatId seatId, CustomerId customerId) {
        return lockingService.executeWithOptimisticLock(seatId, seat -> {
            seat.reserve(customerId);
        });
    }

    @Transactional
    public void releaseSeat(SeatId seatId) {
        lockingService.executeWithOptimisticLock(seatId, Seat::release);
    }

    @Transactional
    public List<Seat> holdSeats(List<SeatId> seatIds, CustomerId customerId) {
        return holdSeats(seatIds, customerId, Duration.ofMinutes(DEFAULT_HOLD_DURATION_MINUTES));
    }

    @Transactional
    public List<Seat> holdSeats(List<SeatId> seatIds, CustomerId customerId, Duration holdDuration) {
        List<Seat> seats = new ArrayList<>();
        for (SeatId seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId);
            if (seat == null) {
                throw new SeatNotAvailableException("Không tìm thấy ghế: " + seatId.value());
            }

            if (seat.getStatus() != SeatStatus.AVAILABLE && !seat.isHoldExpired()) {
                throw new SeatNotAvailableException("Ghế không có sẵn: " + seatId.value());
            }

            seats.add(seat);
        }
        try {
            for (Seat seat : seats) {
                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    if (seat.isHoldExpired()) {
                        seat.release();
                    }
                }
                seat.hold(customerId, holdDuration);
                seatRepository.save(seat);
            }

            return seats;
        } catch (OptimisticLockingFailureException e) {
            throw new SeatLockingException(e.getMessage());
        }
    }

    @Transactional
    public int processExpiredSeats() {
        List<Seat> expiredSeats = seatRepository.findExpiredHolds();

        for (Seat seat : expiredSeats) {
            try {
                seat.release();
                seatRepository.save(seat);
            } catch (OptimisticLockingFailureException e) {
                log.error("Không thể giải phóng ghế {} do xung đột. Sẽ thử lại ở lần chạy tiếp theo.", seat.getSeatId().value());
            }
        }

        return expiredSeats.size();
    }
}