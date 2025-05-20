package vn.edu.hust.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Booking")
public class BookingEntity {
    @Id
    private String id;

    private Long customerId;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "booking_id")
    private Set<SeatReservationEntity> seatReservations = new HashSet<>();
}