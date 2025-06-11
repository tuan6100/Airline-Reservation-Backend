package vn.edu.hust.application.eventhandler;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRES_NEW,
            timeout = 30
    )
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
        bookingEntity.setVersion(0);
        bookingJpaRepository.save(bookingEntity);
    }

    @EventHandler
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRES_NEW,
            timeout = 30
    )
    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void on(TicketAddedToBookingEvent event) {
        BookingEntity entity = bookingJpaRepository.findByIdWithLock(event.bookingId());

        if (entity != null) {
            synchronized (this) {
                entity.setTicketCount(entity.getTicketCount() != null ? entity.getTicketCount() + 1 : 1);
                if (entity.getTotalAmount() != null && event.price() != null) {
                    entity.setTotalAmount(entity.getTotalAmount() + event.price());
                }
                entity.setUpdatedAt(LocalDateTime.now());
                bookingJpaRepository.save(entity);
            }
        }
    }

    @EventHandler
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRES_NEW,
            timeout = 30
    )
    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void on(TicketRemovedFromBookingEvent event) {
        BookingEntity entity = bookingJpaRepository.findByIdWithLock(event.bookingId());

        if (entity != null) {
            synchronized (this) {
                entity.setTicketCount(Math.max(0, entity.getTicketCount() != null ? entity.getTicketCount() - 1 : 0));
                entity.setUpdatedAt(LocalDateTime.now());
                bookingJpaRepository.save(entity);
            }
        }
    }

    @EventHandler
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRES_NEW,
            timeout = 30
    )
    public void on(BookingConfirmedEvent event) {
        BookingEntity entity = bookingJpaRepository.findById(event.bookingId()).orElse(null);
        if (entity != null) {
            entity.setStatus(BookingStatus.CONFIRMED);
            entity.setUpdatedAt(LocalDateTime.now());
            bookingJpaRepository.save(entity);
        }
    }

    @EventHandler
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRES_NEW,
            timeout = 30
    )
    public void on(BookingCancelledEvent event) {
        BookingEntity entity = bookingJpaRepository.findById(event.bookingId()).orElse(null);
        if (entity != null) {
            entity.setStatus(BookingStatus.CANCELLED);
            entity.setUpdatedAt(LocalDateTime.now());
            bookingJpaRepository.save(entity);
        }
    }

    @EventHandler
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRES_NEW,
            timeout = 30
    )
    public void on(BookingExpiredEvent event) {
        BookingEntity entity = bookingJpaRepository.findById(event.bookingId()).orElse(null);
        if (entity != null) {
            entity.setStatus(BookingStatus.EXPIRED);
            entity.setUpdatedAt(LocalDateTime.now());
            bookingJpaRepository.save(entity);
        }
    }

    @EventHandler
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRES_NEW,
            timeout = 30
    )
    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 1.5)
    )
    public void on(SeatHeldEvent event) {
        SeatEntity seatEntity = seatJpaRepository.findByIdWithLock(event.seatId());
        if (seatEntity != null) {
            if (seatEntity.getStatus() == SeatStatus.AVAILABLE) {
                seatEntity.setStatus(SeatStatus.ON_HOLD);
                seatJpaRepository.save(seatEntity);
            } else {
                throw new IllegalStateException("Seat " + event.seatId() + " is not available for holding");
            }
        }
    }

    @EventHandler
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRES_NEW,
            timeout = 30
    )
    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 1.5)
    )
    public void on(SeatReleasedEvent event) {
        SeatEntity seatEntity = seatJpaRepository.findByIdWithLock(event.seatId());
        if (seatEntity != null) {
            seatEntity.setStatus(SeatStatus.AVAILABLE);
            seatJpaRepository.save(seatEntity);
        }
    }
}