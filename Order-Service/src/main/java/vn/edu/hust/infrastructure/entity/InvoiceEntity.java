package vn.edu.hust.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "invoices")
@Getter
@Setter
public class InvoiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount = 0L;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private OrderEntity order;

    @OneToMany(mappedBy = "invoice_id")
    private Set<PaymentEntity> paymentEntities;
}
