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
        bookingEntity.setCreatedAt(java.time.LocalDateTime.now());
        bookingEntity.setExpiresAt(event.expiresAt());
        bookingEntity.setSeatCount(event.seatReservations().size());

        bookingJpaRepository.save(bookingEntity);
    }

    @EventHandler
    public void on(BookingConfirmedEvent event) {
        BookingEntity entity = bookingJpaRepository.findById(event.bookingId()).get();
        entity.setStatus(BookingStatus.CONFIRMED);
        bookingJpaRepository.save(entity);
    }

    @EventHandler
    public void on(BookingCancelledEvent event) {
        BookingEntity entity = bookingJpaRepository.findById(event.bookingId()).get();
        entity.setStatus(BookingStatus.CANCELLED);
        bookingJpaRepository.save(entity);
    }

    @EventHandler
    public void on(SeatHeldEvent event) {
        SeatEntity seatEntity = seatJpaRepository.findById(event.seatId()).get();
        seatEntity.setStatus(SeatStatus.ON_HOLD);
        seatEntity.setHoldUntil(event.holdUntil());
        seatEntity.setHeldByCustomerId(event.customerId());
        seatJpaRepository.save(seatEntity);
    }

    @EventHandler
    public void on(SeatReleasedEvent event) {
        SeatEntity seatEntity = seatJpaRepository.findById(event.seatId()).get();
        seatEntity.setStatus(SeatStatus.AVAILABLE);
        seatEntity.setHoldUntil(null);
        seatEntity.setHeldByCustomerId(null);
        seatJpaRepository.save(seatEntity);
    }
}