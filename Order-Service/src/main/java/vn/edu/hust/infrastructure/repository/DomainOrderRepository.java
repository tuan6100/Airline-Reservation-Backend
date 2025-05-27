package vn.edu.hust.infrastructure.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.aggregate.Order;
import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.CustomerId;
import vn.edu.hust.domain.model.valueobj.OrderId;
import vn.edu.hust.domain.repository.OrderRepository;
import vn.edu.hust.infrastructure.entity.OrderEntity;
import vn.edu.hust.infrastructure.mapper.OrderMapper;

import java.util.List;
import java.util.stream.Collectors;


@Repository
public class DomainOrderRepository implements OrderRepository {

    @Autowired private OrderJpaRepository jpaRepository;

    @Autowired private OrderMapper mapper;

    @Override
    public Order findById(OrderId orderId) {
        return jpaRepository.findById(orderId.value())
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        OrderEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return jpaRepository.findByCustomerId(customerId.value())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Order findByBookingId(BookingId bookingId) {
        return jpaRepository.findByBookingId(bookingId.value())
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}