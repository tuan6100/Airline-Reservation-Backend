package vn.edu.hust.domain.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.edu.hust.domain.exception.BookingExpiredException;
import vn.edu.hust.domain.exception.SeatNotAvailableException;
import vn.edu.hust.domain.model.aggregate.Booking;
import vn.edu.hust.domain.model.aggregate.Seat;
import vn.edu.hust.domain.model.enumeration.CancellationReason;
import vn.edu.hust.domain.model.enumeration.SeatStatus;
import vn.edu.hust.domain.model.valueobj.*;
import vn.edu.hust.domain.repository.BookingRepository;
import vn.edu.hust.domain.repository.SeatRepository;
import vn.edu.hust.infrastructure.event.DomainEventPublisher;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class BookingDomainService {
    @Autowired private BookingRepository bookingRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private DomainEventPublisher eventPublisher;


    public BookingId createBooking(CustomerId customerId, List<SeatSelectionRequest> seatSelections) {
        BookingId bookingId = new BookingId(UUID.randomUUID().toString());
        List<SeatReservation> seatReservations = new ArrayList<>();
        for (SeatSelectionRequest selection : seatSelections) {
            Seat seat = seatRepository.findById(selection.seatId());
            if (seat == null) {
                throw new EntityNotFoundException("Seat not found: " + selection.seatId());
            }
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatNotAvailableException("Seat is not available: " + selection.seatId());
            }
            seat.hold(customerId, Duration.ofMinutes(15));
            seatRepository.save(seat);
            SeatReservation reservation = new SeatReservation(
                    seat.getSeatId(),
                    seat.getFlightId(),
                    SeatClass.fromId(selection.seatClassId()),
                    selection.price()
            );

            seatReservations.add(reservation);
        }
        Booking booking = Booking.create(bookingId, customerId, seatReservations);
        bookingRepository.save(booking);
        return bookingId;
    }

    public void confirmBooking(BookingId bookingId) {
        Booking booking = bookingRepository.findById(bookingId);
        if (booking == null) {
            throw new EntityNotFoundException("Booking not found: " + bookingId);
        }
        if (booking.isExpired()) {
            throw new BookingExpiredException("Booking has expired: " + bookingId);
        }
        booking.confirm();
        for (SeatReservation reservation : booking.getSeatReservations()) {
            Seat seat = seatRepository.findById(reservation.seatId());
            seat.reserve(booking.getCustomerId());
            seatRepository.save(seat);
        }
        bookingRepository.save(booking);
    }

    public void cancelBooking(BookingId bookingId, CancellationReason reason) {
        Booking booking = bookingRepository.findById(bookingId);

        if (booking == null) {
            throw new EntityNotFoundException("Booking not found: " + bookingId);
        }
        booking.cancel(reason);
        for (SeatReservation reservation : booking.getSeatReservations()) {
            Seat seat = seatRepository.findById(reservation.seatId());
            seat.release();
            seatRepository.save(seat);
        }
        bookingRepository.save(booking);
    }

    @Scheduled(fixedRate = 60000)
    public void processExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findPendingBookingsExpiredBefore(now);
        for (Booking booking : expiredBookings) {
            booking.expire();
            for (SeatReservation reservation : booking.getSeatReservations()) {
                Seat seat = seatRepository.findById(reservation.seatId());
                if (seat != null) {
                    seat.release();
                    seatRepository.save(seat);
                }
            }

            bookingRepository.save(booking);
        }
    }

    @Scheduled(fixedRate = 30000)
    public void processExpiredSeatHolds() {
        List<Seat> seatsWithExpiredHolds = seatRepository.findExpiredHolds();

        for (Seat seat : seatsWithExpiredHolds) {
            seat.release();
            seatRepository.save(seat);
        }
    }
}