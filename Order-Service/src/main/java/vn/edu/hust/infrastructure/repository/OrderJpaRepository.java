package vn.edu.hust.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.enumeration.OrderStatus;
import vn.edu.hust.infrastructure.entity.OrderEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByCustomerId(Long customerId);

    Optional<OrderEntity> findByBookingId(String bookingId);

    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status")
    List<OrderEntity> findByStatus(@Param("status") String status);

    @Query("SELECT o FROM OrderEntity o WHERE o.paymentStatus = :paymentStatus")
    List<OrderEntity> findByPaymentStatus(@Param("paymentStatus") String paymentStatus);

    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    List<OrderEntity> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM OrderEntity o WHERE o.bookingId = :bookingId")
    boolean existsByBookingId(@Param("bookingId") String bookingId);


    @Modifying
    @Query(value = "INSERT INTO ticket_order (customer_id, booking_id, status, payment_status, " +
            "total_amount, currency, created_at, updated_at, version) " +
            "SELECT :customerId, :bookingId, :status, :paymentStatus, :totalAmount, :currency, " +
            ":createdAt, :updatedAt, 0 " +
            "WHERE NOT EXISTS (SELECT 1 FROM \"TicketOrder\" WHERE booking_id = :bookingId)",
            nativeQuery = true)
    int createOrderIfNotExists(
            @Param("customerId") Long customerId,
            @Param("bookingId") String bookingId,
            @Param("status") String status,
            @Param("paymentStatus") String paymentStatus,
            @Param("totalAmount") java.math.BigDecimal totalAmount,
            @Param("currency") String currency,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying
    @Query("UPDATE OrderEntity o SET o.status = :newStatus, o.updatedAt = :updateTime, o.version = o.version + 1 " +
            "WHERE o.orderId = :orderId AND o.status = :currentStatus AND o.version = :version")
    int updateOrderStatusAtomic(
            @Param("orderId") Long orderId,
            @Param("currentStatus") String currentStatus,
            @Param("newStatus") String newStatus,
            @Param("version") Integer version,
            @Param("updateTime") LocalDateTime updateTime
    );
}
