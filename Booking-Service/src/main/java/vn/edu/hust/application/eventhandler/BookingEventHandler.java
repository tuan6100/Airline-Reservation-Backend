package vn.edu.hust.application.eventhandler;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.domain.model.enumeration.BookingStatus;
import vn.edu.hust.domain.model.enumeration.SeatStatus;
import vn.edu.hust.infrastructure.entity.BookingEntity;
import vn.edu.hust.infrastructure.entity.SeatEntity;
import vn.edu.hust.infrastructure.repository.BookingJpaRepository;
import vn.edu.hust.infrastructure.repository.SeatJpaRepository;

import java.time.LocalDateTime;

@Component
public class BookingEventHandler {

    @Autowired
    private BookingJpaRepository bookingJpaRepository;

    @Autowired
    private SeatJpaRepository seatJpaRepository;

    @EventHandler
    public void on(BookingCreatedEvent event) {
        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setBookingId(event.bookingId());
        bookingEntity.setCustomerId(event.customerId());
        bookingEntity.setStatus(BookingStatus.PENDING);
        bookingEntity.setCreatedAt(LocalDateTime.now());
        bookingEntity.setExpiresAt(event.expiresAt());
        bookingEntity.setFlightId(event.flightId());
        bookingEntity.setFlightDepartureTime(event.flightDepartureTime());
        bookingEntity.setTotalAmount(event.totalAmount());
        bookingEntity.setCurrency(event.currency());
        bookingEntity.setSeatCount(event.seatReservations().size());
        bookingEntity.setTicketCount(0);
        bookingJpaRepository.save(bookingEntity);
    }

    @EventHandler
    public void on(TicketAddedToBookingEvent event) {
        BookingEntity entity = bookingJpaRepository.findById(event.bookingId()).orElse(null);
        if (entity != null) {
            entity.setTicketCount(entity.getTicketCount() != null ? entity.getTicketCount() + 1 : 1);
            if (entity.getTotalAmount() != null && event.price() != null) {
                entity.setTotalAmount(entity.getTotalAmount() + event.price());
            }

            bookingJpaRepository.save(entity);
        }
    }

    @EventHandler
    public void on(TicketRemovedFromBookingEvent event) {
        BookingEntity entity = bookingJpaRepository.findById(event.bookingId()).orElse(null);
        if (entity != null) {
            entity.setTicketCount(Math.max(0, entity.getTicketCount() != null ? entity.getTicketCount() - 1 : 0));
            bookingJpaRepository.save(entity);
        }
    }

    @EventHandler
    public void on(BookingConfirmedEvent event) {
        BookingEntity entity = bookingJpaRepository.findById(event.bookingId()).orElse(null);
        if (entity != null) {
            entity.setStatus(BookingStatus.CONFIRMED);
            bookingJpaRepository.save(entity);
        }
    }

    @EventHandler
    public void on(BookingCancelledEvent event) {
        BookingEntity entity = bookingJpaRepository.findById(event.bookingId()).orElse(null);
        if (entity != null) {
            entity.setStatus(BookingStatus.CANCELLED);
            bookingJpaRepository.save(entity);
        }
    }

    @EventHandler
    public void on(BookingExpiredEvent event) {
        BookingEntity entity = bookingJpaRepository.findById(event.bookingId()).orElse(null);
        if (entity != null) {
            entity.setStatus(BookingStatus.EXPIRED);
            bookingJpaRepository.save(entity);
        }
    }

    @EventHandler
    public void on(SeatHeldEvent event) {
        SeatEntity seatEntity = seatJpaRepository.findById(event.seatId()).orElse(null);
        if (seatEntity != null) {
            seatEntity.setStatus(SeatStatus.ON_HOLD);
            seatJpaRepository.save(seatEntity);
        }
    }

    @EventHandler
    public void on(SeatReleasedEvent event) {
        SeatEntity seatEntity = seatJpaRepository.findById(event.seatId()).orElse(null);
        if (seatEntity != null) {
            seatEntity.setStatus(SeatStatus.AVAILABLE);
            seatJpaRepository.save(seatEntity);
        }
    }
}