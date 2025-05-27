package vn.edu.hust.infrastructure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.infrastructure.exception.BookingLockingException;
import vn.edu.hust.domain.exception.SeatLockingException;
import vn.edu.hust.domain.model.aggregate.Booking;
import vn.edu.hust.domain.model.aggregate.Seat;
import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.SeatId;
import vn.edu.hust.domain.repository.BookingRepository;
import vn.edu.hust.domain.repository.SeatRepository;

import java.util.function.Consumer;

@Service
public class OptimisticLockingService {

    @Autowired private SeatRepository seatRepository;
    @Autowired private BookingRepository bookingRepository;
    private static final int MAX_RETRIES = 3;

    @Transactional
    public Seat executeWithOptimisticLock(SeatId seatId, Consumer<Seat> updateOperation) {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                Seat seat = seatRepository.findById(seatId);
                if (seat == null) {
                    throw new SeatLockingException("Không tìm thấy ghế có ID: " + seatId.value());
                }
                updateOperation.accept(seat);
                seatRepository.save(seat);
                return seat;
            } catch (OptimisticLockingFailureException e) {
                retries++;
                if (retries >= MAX_RETRIES) {
                    throw new SeatLockingException("Không thể cập nhật ghế sau " + MAX_RETRIES + " lần thử lại");
                }
                try {
                    Thread.sleep(100L * retries);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new SeatLockingException("Không thể cập nhật ghế do xung đột");
    }

    @Transactional
    public Booking executeWithOptimisticLock(BookingId bookingId, Consumer<Booking> updateOperation) {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                Booking booking = bookingRepository.findById(bookingId);
                if (booking == null) {
                    throw new BookingLockingException("Không tìm thấy đặt chỗ có ID: " + bookingId.value());
                }
                updateOperation.accept(booking);
                bookingRepository.save(booking);
                return booking;
            } catch (OptimisticLockingFailureException e) {
                retries++;
                if (retries >= MAX_RETRIES) {
                    throw new BookingLockingException("Không thể cập nhật đặt chỗ sau " + MAX_RETRIES + " lần thử lại", e);
                }
                try {
                    Thread.sleep(100L * retries);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new BookingLockingException("Không thể cập nhật đặt chỗ do xung đột");
    }
}
