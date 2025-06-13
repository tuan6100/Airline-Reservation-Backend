package vn.edu.hust.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.hust.domain.model.enumeration.OrderStatus;
import vn.edu.hust.domain.model.enumeration.PaymentStatus;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "booking_id", unique = true)
    private String bookingId;

    @Column(name = "customer_id")
    private Long customerId;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private PromotionEntity promotion;

    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private OrderStatus orderStatus;

    @Column(name = "payment_status")
    @Enumerated(EnumType.ORDINAL)
    private PaymentStatus paymentStatus;

    @Column(name = "total_price")
    private Long totalPrice = 0L;

    @Column(name = "currency")
    private String currency;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order_id", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private Set<OrderItemEntity> orderItemEntities = new HashSet<>();

    @Version
    @Column(name = "version")
    private Integer version = 0;

}
