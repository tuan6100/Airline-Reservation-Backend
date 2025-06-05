package vn.edu.hust.application.eventhandler;

import jakarta.persistence.LockModeType;
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
import vn.edu.hust.domain.model.enumeration.TicketStatus;
import vn.edu.hust.infrastructure.entity.TicketEntity;
import vn.edu.hust.infrastructure.repository.SeatJpaRepository;
import vn.edu.hust.infrastructure.repository.TicketJpaRepository;

import java.time.LocalDateTime;

@Component
public class TicketEventHandler {

    @Autowired
    private TicketJpaRepository ticketJpaRepository;

    @Autowired
    private SeatJpaRepository seatJpaRepository;

    @EventHandler
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRES_NEW,
            timeout = 30
    )
    public void on(TicketCreatedEvent event) {
        TicketEntity ticketEntity = new TicketEntity();
        ticketEntity.setTicketId(event.ticketId());
        ticketEntity.setTicketCode(event.ticketCode());
        ticketEntity.setFlightId(event.flightId());
        ticketEntity.setFlightDepartureTime(event.flightDepartureTime());
        ticketEntity.setSeat(seatJpaRepository.findById(event.seatId()).orElseThrow(
                () -> new IllegalArgumentException("Seat not found: " + event.seatId())
        ));
        ticketEntity.setStatus(event.status());
        ticketEntity.setCreatedAt(event.createdAt());
        ticketEntity.setVersion(0); // Initialize version

        ticketJpaRepository.save(ticketEntity);
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
    public void on(TicketHeldEvent event) {
        TicketEntity ticketEntity = ticketJpaRepository.findByIdWithPessimisticLock(event.ticketId());
        if (ticketEntity != null) {
            if (ticketEntity.getStatus() == TicketStatus.AVAILABLE) {
                ticketEntity.setStatus(TicketStatus.HELD);
                ticketJpaRepository.save(ticketEntity);
            } else {
                throw new IllegalStateException("Ticket " + event.ticketId() + " is not available for holding");
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
    public void on(TicketBookedEvent event) {
        TicketEntity ticketEntity = ticketJpaRepository.findByIdWithPessimisticLock(event.ticketId());

        if (ticketEntity != null) {
            if (ticketEntity.getStatus() == TicketStatus.HELD ||
                    ticketEntity.getStatus() == TicketStatus.AVAILABLE) {
                ticketEntity.setStatus(TicketStatus.BOOKED);
                ticketEntity.setBookingId(event.bookingId());
                ticketJpaRepository.save(ticketEntity);
            } else {
                throw new IllegalStateException("Ticket " + event.ticketId() + " cannot be booked in current state: " + ticketEntity.getStatus());
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
    public void on(TicketReleasedEvent event) {
        TicketEntity ticketEntity = ticketJpaRepository.findByIdWithPessimisticLock(event.ticketId());

        if (ticketEntity != null) {
            ticketEntity.setStatus(TicketStatus.AVAILABLE);
            ticketEntity.setBookingId(null);
            ticketJpaRepository.save(ticketEntity);
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
    public void on(TicketCancelledEvent event) {
        TicketEntity ticketEntity = ticketJpaRepository.findByIdWithPessimisticLock(event.ticketId());

        if (ticketEntity != null) {
            ticketEntity.setStatus(TicketStatus.CANCELLED);
            ticketEntity.setUpdatedAt(LocalDateTime.now());
            ticketJpaRepository.save(ticketEntity);
        }
    }
}