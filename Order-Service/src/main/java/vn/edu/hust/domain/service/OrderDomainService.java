package vn.edu.hust.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.hust.domain.exception.OrderNotFoundException;
import vn.edu.hust.domain.model.aggregate.Order;
import vn.edu.hust.domain.model.entity.OrderItem;
import vn.edu.hust.domain.model.enumeration.OrderStatus;
import vn.edu.hust.domain.model.valueobj.*;
import vn.edu.hust.domain.repository.OrderRepository;
import vn.edu.hust.infrastructure.event.DomainEventPublisher;

import java.util.List;


@Service
public class OrderDomainService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DomainEventPublisher eventPublisher;


    public OrderId createOrder(BookingId bookingId, CustomerId customerId, PromotionId promotionId) {
        Order existingOrder = orderRepository.findByBookingId(bookingId);
        if (existingOrder != null) {
            if (existingOrder.getStatus() == OrderStatus.PENDING) {
                return existingOrder.getOrderId();
            }
            return existingOrder.getOrderId();
        }
        Order order = Order.create(customerId, bookingId, promotionId);
        Order savedOrder = orderRepository.save(order);
        savedOrder.afterCreate();
        return savedOrder.getOrderId();
    }


    public void addOrderItem(OrderId orderId, TicketId ticketId, FlightId flightId,
                             SeatId seatId, Money price, String description) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        OrderItem item = OrderItem.create(ticketId, flightId, seatId, price, description);
        order.addItem(item);

        orderRepository.save(order);
    }

    public void confirmOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        order.confirm();
        orderRepository.save(order);
    }

    public void cancelOrder(OrderId orderId, String reason) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        order.cancel(reason);
        orderRepository.save(order);
    }

    public void markPaymentPending(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        order.markPaymentPending();
        orderRepository.save(order);
    }

    public void markOrderPaid(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        order.markPaid();
        orderRepository.save(order);
    }

    public void markOrderRefunded(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        order.markRefunded();
        orderRepository.save(order);
    }

    public List<Order> getOrdersByCustomer(CustomerId customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public Order getOrderById(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }
        return order;
    }
}