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

    @Column(name = "status")
    private BookingStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @OneToMany()
    @JoinColumn(name = "ticket_id")
    private Set<TicketEntity> tickets;


}