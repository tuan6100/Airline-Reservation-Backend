package vn.edu.hust.domain.model.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.hust.domain.model.enumeration.TicketStatus;
import vn.edu.hust.domain.model.valueobj.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Ticket {
    private TicketId ticketId;
    private UUID ticketCode;
    private FlightId flightId;
    private LocalDateTime flightDepartureTime;
    private SeatId seatId;
    private LocalDateTime createdAt;
    private TicketStatus status;
    private SeatDetails seatDetails;
    
    public static Ticket create(FlightId flightId, LocalDateTime departureTime, SeatId seatId) {
        Ticket ticket = new Ticket();
        ticket.ticketCode = UUID.randomUUID();
        ticket.flightId = flightId;
        ticket.flightDepartureTime = departureTime;
        ticket.seatId = seatId;
        ticket.createdAt = LocalDateTime.now();
        ticket.status = TicketStatus.AVAILABLE;
        return ticket;
    }

    public void hold() {
        if (status != TicketStatus.AVAILABLE) {
            throw new IllegalStateException("Ticket must be available to hold");
        }
        status = TicketStatus.HELD;
    }

    public void book() {
        if (status != TicketStatus.HELD && status != TicketStatus.AVAILABLE) {
            throw new IllegalStateException("Ticket must be available or held to book");
        }
        status = TicketStatus.BOOKED;
    }

    public void cancel() {
        status = TicketStatus.CANCELLED;
    }

    public void release() {
        status = TicketStatus.AVAILABLE;
    }
}
