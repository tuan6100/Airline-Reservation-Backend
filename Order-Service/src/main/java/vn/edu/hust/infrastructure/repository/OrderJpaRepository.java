package vn.edu.hust.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.enumeration.OrderStatus;
import vn.edu.hust.infrastructure.entity.OrderEntity;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {



    List<OrderEntity> findByCustomerId(Long customerId);

    Optional<OrderEntity> findByBookingId(String bookingId); // String for booking ID

    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status")
    List<OrderEntity> findByStatus(@Param("status") String status); // String for status

    @Query("SELECT o FROM OrderEntity o WHERE o.paymentStatus = :paymentStatus")
    List<OrderEntity> findByPaymentStatus(@Param("paymentStatus") String paymentStatus);

    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    List<OrderEntity> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                             @Param("endDate") java.time.LocalDateTime endDate);
}
