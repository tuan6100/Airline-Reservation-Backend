package vn.edu.hust.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.hust.infrastructure.dto.ResetTimeoutRequest;
import vn.edu.hust.infrastructure.entity.OrderEntity;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByCustomerId(Long customerId);

    Optional<OrderEntity> findByBookingId(String bookingId);

    @Query("""
        SELECT o.customerId FROM OrderEntity o WHERE o.orderId = ?1
        """)
    Optional<Long> findCustomerIdByOrderId(Long orderId);

    @Query("""
    SELECT new vn.edu.hust.infrastructure.dto.ResetTimeoutRequest(t.ticketId, t.seat.seatId)
    FROM OrderItemEntity o
    JOIN FETCH TicketEntity t ON o.ticketId = t.ticketId
    JOIN FETCH SeatEntity s ON t.seat.seatId = s.seatId
    WHERE o.orderEntity.orderId = ?1
    """)
    Optional<ResetTimeoutRequest> findSeatAndTicketByOrderId(Long orderId);
}
