package vn.edu.hust.infrastructure.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.aggregate.Ticket;
import vn.edu.hust.domain.model.valueobj.FlightId;
import vn.edu.hust.domain.model.valueobj.SeatId;
import vn.edu.hust.domain.model.valueobj.TicketId;
import vn.edu.hust.domain.repository.TicketRepository;
import vn.edu.hust.infrastructure.mapper.TicketMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DomainTicketRepository implements TicketRepository {

    @Autowired
    private TicketJpaRepository jpaRepository;

    @Autowired
    private TicketMapper mapper;

    @Override
    public Ticket findById(TicketId ticketId) {
        return jpaRepository.findById(ticketId.value())
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Ticket save(Ticket ticket) {
        var entity = mapper.toEntity(ticket);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<Ticket> findByFlightId(FlightId flightId) {
        return jpaRepository.findByFlightId(flightId.value())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findAvailableByFlightId(FlightId flightId) {
        return jpaRepository.findAvailableByFlightId(flightId.value())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Ticket findBySeatId(SeatId seatId) {
        var entity = jpaRepository.findBySeatId(seatId.value());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<Ticket> findExpiredHolds() {
        return jpaRepository.findExpiredHolds(LocalDateTime.now())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}