package vn.edu.hust.infrastructure.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.aggregate.Order;
import vn.edu.hust.domain.model.enumeration.OrderStatus;
import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.CustomerId;
import vn.edu.hust.domain.model.valueobj.OrderId;
import vn.edu.hust.infrastructure.mapper.OrderMapper;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OrderQueryRepository {

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    private BookedTicketJpaRepository bookedTicketJpaRepository;

    @Autowired
    private OrderMapper orderMapper;

    public Order findById(OrderId orderId) {
        return orderJpaRepository.findById(orderId.value())
                .map(orderMapper::toDomain)
                .orElse(null);
    }

    public List<Order> findByCustomerId(CustomerId customerId) {
        return orderJpaRepository.findByCustomerId(customerId.value())
                .stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }

    public Order findByBookingId(BookingId bookingId) {
        return orderJpaRepository.findByBookingId(bookingId.value())
                .map(orderMapper::toDomain)
                .orElse(null);
    }

    public List<Order> findAll() {
        return orderJpaRepository.findAll()
                .stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Order> findByStatus(String status) {
        return orderJpaRepository.findByStatus(status)
                .stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }
}
