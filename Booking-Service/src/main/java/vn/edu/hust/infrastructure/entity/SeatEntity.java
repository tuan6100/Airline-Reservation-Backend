package vn.edu.hust.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.hust.domain.model.enumeration.SeatStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"Seat\"")
public class SeatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_class_id")
    private SeatClassEntity seatClass;

    @Column(name = "aircraft_id", nullable = false)
    private Long aircraftId;

    @Column(name = "seat_code", nullable = false)
    private String seatCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Version
    @Column(name = "version")
    private Integer version = 0;

    public Long getSeatClassId() {
        return seatClass != null ? seatClass.getId().longValue() : null;
    }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }
}