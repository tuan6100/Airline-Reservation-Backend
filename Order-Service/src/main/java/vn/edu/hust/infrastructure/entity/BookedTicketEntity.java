package vn.edu.hust.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "\"BookedTicket\"")
@Getter
@Setter
public class BookedTicketEntity {
    @Id
    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", insertable = false, updatable = false)
    private TicketEntity ticket;
}
