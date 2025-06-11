package vn.edu.hust.infrastructure.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "\"seat_classes\"")
public class SeatClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_class_id", nullable = false)
    private Integer id;

    @Column(name = "seat_class_name", nullable = false, length = Integer.MAX_VALUE)
    private String seatClassName;

    private Long airlineId;

    @NotNull
    @Column(name = "price", nullable = false)
    private Integer price;

}
