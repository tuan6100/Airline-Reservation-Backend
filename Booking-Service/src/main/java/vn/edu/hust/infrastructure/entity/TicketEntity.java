package vn.edu.hust.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.hust.domain.model.enumeration.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"Ticket\"")
public class TicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "ticket_code")
    private UUID ticketCode;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Column(name = "flight_departure_time")
    private LocalDateTime flightDepartureTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    private TicketStatus status = TicketStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", insertable = false, updatable = false)
    private SeatEntity seat;

    @Column(name = "booking_id")
    private String bookingId;

    public Long getSeatId() {
        return seat != null ? seat.getSeatId() : null;
    }
}