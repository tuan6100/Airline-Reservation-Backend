package vn.edu.hust.application.eventhandler;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.edu.hust.domain.event.*;
import vn.edu.hust.domain.model.enumeration.TicketStatus;
import vn.edu.hust.infrastructure.entity.TicketEntity;
import vn.edu.hust.infrastructure.repository.SeatJpaRepository;
import vn.edu.hust.infrastructure.repository.TicketJpaRepository;

@Component
public class TicketEventHandler {

    @Autowired
    private TicketJpaRepository ticketJpaRepository;

    @Autowired
    private SeatJpaRepository seatJpaRepository;

    @EventHandler
    public void on(TicketCreatedEvent event) {
        TicketEntity ticketEntity = new TicketEntity();
        ticketEntity.setTicketId(event.ticketId());
        ticketEntity.setTicketCode(event.ticketCode());
        ticketEntity.setFlightId(event.flightId());
        ticketEntity.setFlightDepartureTime(event.flightDepartureTime());
        ticketEntity.setSeat(seatJpaRepository.findById(event.seatId()).get());
        ticketEntity.setStatus(event.status());
        ticketEntity.setCreatedAt(event.createdAt());
        ticketJpaRepository.save(ticketEntity);
    }

    @EventHandler
    public void on(TicketHeldEvent event) {
        TicketEntity ticketEntity = ticketJpaRepository.findById(event.ticketId()).get();
        ticketEntity.setStatus(TicketStatus.HELD);
        ticketJpaRepository.save(ticketEntity);
    }

    @EventHandler
    public void on(TicketBookedEvent event) {
        TicketEntity ticketEntity = ticketJpaRepository.findByTicketId(event.ticketId());
        if (ticketEntity != null) {
            ticketEntity.setStatus(TicketStatus.BOOKED);
            ticketEntity.setBookingId(event.bookingId());
            ticketJpaRepository.save(ticketEntity);
        }
    }

    @EventHandler
    public void on(TicketReleasedEvent event) {
        TicketEntity ticketEntity = ticketJpaRepository.findByTicketId(event.ticketId());
        if (ticketEntity != null) {
            ticketEntity.setStatus(TicketStatus.AVAILABLE);
            ticketEntity.setBookingId(null);
            ticketJpaRepository.save(ticketEntity);
        }
    }

    @EventHandler
    public void on(TicketCancelledEvent event) {
        TicketEntity ticketEntity = ticketJpaRepository.findByTicketId(event.ticketId());
        if (ticketEntity != null) {
            ticketEntity.setStatus(TicketStatus.CANCELLED);
            ticketJpaRepository.save(ticketEntity);
        }
    }
}