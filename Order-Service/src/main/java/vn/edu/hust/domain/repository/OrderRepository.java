package vn.edu.hust.domain.repository;

import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.aggregate.Order;
import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.CustomerId;
import vn.edu.hust.domain.model.valueobj.OrderId;

import java.util.List;

/**
 * Repository interface for Order aggregate
 */
@Repository
public interface OrderRepository {
    Order findById(OrderId orderId);
    Order save(Order order);
    List<Order> findByCustomerId(CustomerId customerId);
    Order findByBookingId(BookingId bookingId);
    List<Order> findAll();
}