package vn.edu.hust.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Seat")
public class SeatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @Column(name = "seat_class_id", nullable = false)
    private Long seatClassId;

    @Column(name = "aircraft_id", nullable = false)
    private Long aircraftId;

    @Column(name = "seat_code", nullable = false)
    private String seatCode;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "hold_until")
    private LocalDateTime holdUntil;

    @Version
    @Column(name = "version")
    private Integer version = 0;
}