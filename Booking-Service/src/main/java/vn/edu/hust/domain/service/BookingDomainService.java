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

// Domain Service for booking workflow
@Service
public class BookingDomainService {
    @Autowired private BookingRepository bookingRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private DomainEventPublisher eventPublisher;


    // Create a new booking with seat selections
    public BookingId createBooking(CustomerId customerId, List<SeatSelectionRequest> seatSelections) {
        // Generate new booking ID
        BookingId bookingId = new BookingId(UUID.randomUUID().toString());

        // Validate seat selections and create reservations
        List<SeatReservation> seatReservations = new ArrayList<>();

        for (SeatSelectionRequest selection : seatSelections) {
            Seat seat = seatRepository.findById(selection.seatId());

            if (seat == null) {
                throw new EntityNotFoundException("Seat not found: " + selection.seatId());
            }

            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatNotAvailableException("Seat is not available: " + selection.seatId());
            }

            // Hold the seat
            seat.hold(customerId, Duration.ofMinutes(15));
            seatRepository.save(seat);

            // Create seat reservation
            SeatReservation reservation = new SeatReservation(
                    seat.getSeatId(),
                    seat.getFlightId(),
                    SeatClass.fromId(selection.seatClassId()),
                    selection.price()
            );

            seatReservations.add(reservation);
        }

        // Create booking aggregate
        Booking booking = Booking.create(bookingId, customerId, seatReservations);

        // Save booking
        bookingRepository.save(booking);

        return bookingId;
    }

    // Confirm a booking after payment
    public void confirmBooking(BookingId bookingId) {
        Booking booking = bookingRepository.findById(bookingId);

        if (booking == null) {
            throw new EntityNotFoundException("Booking not found: " + bookingId);
        }

        // Check if booking is expired
        if (booking.isExpired()) {
            throw new BookingExpiredException("Booking has expired: " + bookingId);
        }

        // Confirm booking
        booking.confirm();

        // Permanently reserve seats
        for (SeatReservation reservation : booking.getSeatReservations()) {
            Seat seat = seatRepository.findById(reservation.seatId());
            seat.reserve(booking.getCustomerId());
            seatRepository.save(seat);
        }

        // Save booking
        bookingRepository.save(booking);
    }

    // Cancel a booking
    public void cancelBooking(BookingId bookingId, CancellationReason reason) {
        Booking booking = bookingRepository.findById(bookingId);

        if (booking == null) {
            throw new EntityNotFoundException("Booking not found: " + bookingId);
        }

        // Cancel booking
        booking.cancel(reason);

        // Release seats
        for (SeatReservation reservation : booking.getSeatReservations()) {
            Seat seat = seatRepository.findById(reservation.seatId());
            seat.release();
            seatRepository.save(seat);
        }

        // Save booking
        bookingRepository.save(booking);
    }

    // Process expired bookings
    @Scheduled(fixedRate = 60000) // Run every minute
    public void processExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findPendingBookingsExpiredBefore(now);

        for (Booking booking : expiredBookings) {
            booking.expire();

            // Release seats
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

    // Process expired seat holds
    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    public void processExpiredSeatHolds() {
        List<Seat> seatsWithExpiredHolds = seatRepository.findExpiredHolds();

        for (Seat seat : seatsWithExpiredHolds) {
            seat.release();
            seatRepository.save(seat);
        }
    }
}