package vn.edu.hust.application.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.application.dto.command.CreateBookingCommand;
import vn.edu.hust.application.dto.query.BookingDTO;
import vn.edu.hust.application.dto.query.SeatDTO;
import vn.edu.hust.application.dto.query.SeatReservationDTO;
import vn.edu.hust.domain.model.aggregate.Booking;
import vn.edu.hust.domain.model.aggregate.Seat;
import vn.edu.hust.domain.model.enumeration.CancellationReason;
import vn.edu.hust.domain.model.valueobj.*;
import vn.edu.hust.domain.repository.BookingRepository;
import vn.edu.hust.domain.repository.SeatRepository;
import vn.edu.hust.domain.service.BookingDomainService;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingApplicationService {
    @Autowired private BookingDomainService bookingDomainService;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private FlightService flightService; // External service

    // Create a booking
    @Transactional
    public BookingDTO createBooking(CreateBookingCommand command) {
        // Convert command to domain objects
        CustomerId customerId = new CustomerId(command.getCustomerId());
        List<SeatSelectionRequest> seatSelections = command.getSeatSelections().stream()
                .map(s -> new SeatSelectionRequest(
                        new SeatId(s.getSeatId()),
                        new Money(s.getAmount(), Currency.getInstance(s.getCurrency()))
                ))
                .collect(Collectors.toList());

        // Call domain service
        BookingId bookingId = bookingDomainService.createBooking(customerId, seatSelections);

        // Retrieve and convert to DTO
        Booking booking = bookingRepository.findById(bookingId);
        return convertToDTO(booking);
    }

    // Confirm a booking after payment
    @Transactional
    public BookingDTO confirmBooking(String bookingId) {
        bookingDomainService.confirmBooking(new BookingId(bookingId));

        Booking booking = bookingRepository.findById(new BookingId(bookingId));
        return convertToDTO(booking);
    }

    // Cancel a booking
    @Transactional
    public BookingDTO cancelBooking(String bookingId, String reason) {
        CancellationReason cancellationReason = CancellationReason.valueOf(reason);
        bookingDomainService.cancelBooking(new BookingId(bookingId), cancellationReason);

        Booking booking = bookingRepository.findById(new BookingId(bookingId));
        return convertToDTO(booking);
    }

    // Get booking by ID
    @Transactional(readOnly = true)
    public BookingDTO getBooking(String bookingId) {
        Booking booking = bookingRepository.findById(new BookingId(bookingId));

        if (booking == null) {
            throw new EntityNotFoundException("Booking not found: " + bookingId);
        }

        return convertToDTO(booking);
    }

    // Get all bookings for a customer
    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByCustomer(Long customerId) {
        List<Booking> bookings = bookingRepository.findByCustomerId(new CustomerId(customerId));
        return bookings.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get available seats for a flight
    @Transactional(readOnly = true)
    public List<SeatDTO> getAvailableSeats(Long flightId) {
        List<Seat> availableSeats = seatRepository.findAvailableByFlightId(new FlightId(flightId));
        return availableSeats.stream().map(this::convertToSeatDTO).collect(Collectors.toList());
    }

    // Helper methods for DTO conversion
    private BookingDTO convertToDTO(Booking booking) {
        // Implementation of conversion from domain model to DTO
        return new BookingDTO(
                booking.getBookingId().value(),
                booking.getCustomerId().value(),
                booking.getSeatReservations().stream()
                        .map(this::convertToSeatReservationDTO)
                        .collect(Collectors.toList()),
                booking.getStatus().name(),
                booking.getCreatedAt(),
                booking.getExpiresAt()
        );
    }

    private SeatReservationDTO convertToSeatReservationDTO(SeatReservation reservation) {
        return new SeatReservationDTO(
                reservation.seatId().value(),
                reservation.flightId().value(),
                reservation.seatClass().getName(),
                reservation.price().amount().doubleValue(),
                reservation.price().currency().getCurrencyCode()
        );
    }

    private SeatDTO convertToSeatDTO(Seat seat) {
        return new SeatDTO(
                seat.getSeatId().value(),
                seat.getFlightId().value(),
                seat.getSeatClassId().value(),
                seat.getStatus().name()
        );
    }
}
