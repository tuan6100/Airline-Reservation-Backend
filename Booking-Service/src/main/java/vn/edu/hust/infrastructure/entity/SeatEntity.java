package vn.edu.hust.infrastructure.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
    private Long id;

    private Long flightId;

    private Long aircraftId;

    private Long seatClassId;

    private String seatCode;

    private String status;

    private LocalDateTime holdUntil;

    @Version
    private Integer version;

    private Boolean isAvailable = true;
}