package vn.edu.hust.domain.service;

import org.springframework.stereotype.Service;
import vn.edu.hust.domain.model.aggregate.Booking;
import vn.edu.hust.domain.model.valueobj.Money;
import vn.edu.hust.domain.model.valueobj.SeatReservation;
import vn.edu.hust.domain.model.valueobj.TicketReservation;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

@Service
public class BookingDomainService {

    public Money calculateTotalAmount(Set<SeatReservation> seatReservations, Set<TicketReservation> ticketReservations, String currency) {
        BigDecimal total = BigDecimal.ZERO;

        // Add seat costs
        for (SeatReservation seat : seatReservations) {
            if (seat.amount() != null) {
                total = total.add(BigDecimal.valueOf(seat.amount()));
            }
        }

        // Add ticket costs
        for (TicketReservation ticket : ticketReservations) {
            if (ticket.getPrice() != null) {
                total = total.add(BigDecimal.valueOf(ticket.getPrice()));
            }
        }

        return new Money(total, Currency.getInstance(currency != null ? currency : "VND"));
    }

    public boolean isBookingValid(Booking booking) {
        // Business rules validation
        if (booking.getCustomerId() == null || booking.getCustomerId() <= 0) {
            return false;
        }

        if (booking.getFlightId() == null || booking.getFlightId() <= 0) {
            return false;
        }

        if (booking.getSeatReservations().isEmpty()) {
            return false;
        }

        // Ensure seat count matches ticket count for confirmed bookings
        if (booking.getStatus() == vn.edu.hust.domain.model.enumeration.BookingStatus.CONFIRMED) {
            return booking.getSeatReservations().size() == booking.getTicketReservations().size();
        }

        return true;
    }

    public boolean canAddTicket(Booking booking, Long seatId) {
        // Can only add tickets to pending bookings
        if (booking.getStatus() != vn.edu.hust.domain.model.enumeration.BookingStatus.PENDING) {
            return false;
        }

        // Check if seat is already reserved in this booking
        return booking.getSeatReservations().stream()
                .anyMatch(sr -> sr.seatId().equals(seatId));
    }

    public boolean canRemoveTicket(Booking booking, Long ticketId) {
        // Can only remove tickets from pending bookings
        if (booking.getStatus() != vn.edu.hust.domain.model.enumeration.BookingStatus.PENDING) {
            return false;
        }

        // Check if ticket exists in booking
        return booking.hasTicket(ticketId);
    }
}