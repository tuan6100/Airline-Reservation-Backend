package vn.edu.hust.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItemEntity {

    @Id
    private Long ticketId;

    @ManyToOne()
    @JoinColumn(name = "order_id")
    private OrderEntity orderEntity;

    @Column(name = "added_at")
    private LocalDateTime added_at;
}
