package vn.edu.hust.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.hust.domain.model.enumeration.BookingStatus;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"Booking\"")
public class BookingEntity {
    @Id
    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "customer_id")
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "flight_id")
    private Long flightId;

    @Column(name = "flight_departure_time")
    private LocalDateTime flightDepartureTime;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "seat_count")
    private Integer seatCount;

    @Column(name = "ticket_count")
    private Integer ticketCount;

    @OneToMany(mappedBy = "bookingId", fetch = FetchType.LAZY)
    private Set<TicketEntity> tickets;

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}