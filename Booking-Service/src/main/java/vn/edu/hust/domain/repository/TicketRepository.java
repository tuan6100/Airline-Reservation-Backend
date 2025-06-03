package vn.edu.hust.domain.repository;

import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.aggregate.Ticket;
import vn.edu.hust.domain.model.valueobj.FlightId;
import vn.edu.hust.domain.model.valueobj.SeatId;
import vn.edu.hust.domain.model.valueobj.TicketId;

import java.util.List;

@Repository
public interface TicketRepository {
    Ticket findById(TicketId ticketId);
    Ticket save(Ticket ticket);
    List<Ticket> findByFlightId(FlightId flightId);
    List<Ticket> findAvailableByFlightId(FlightId flightId);
    Ticket findBySeatId(SeatId seatId);
    List<Ticket> findExpiredHolds();
}
