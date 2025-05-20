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

/**
 * Domain service for Order business logic
 */
@Service
public class OrderDomainService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DomainEventPublisher eventPublisher;

    /**
     * Create a new order for a booking
     */
    public OrderId createOrder(BookingId bookingId, CustomerId customerId, PromotionId promotionId) {
        // Check if order already exists for this booking
        Order existingOrder = orderRepository.findByBookingId(bookingId);
        if (existingOrder != null) {
            // Return existing order if it's in PENDING status
            if (existingOrder.getStatus() == OrderStatus.PENDING) {
                return existingOrder.getOrderId();
            }
            // Otherwise, we might want to handle this case differently
            // For now, let's just return the existing order ID
            return existingOrder.getOrderId();
        }

        // Create new order
        Order order = Order.create(customerId, bookingId, promotionId);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Trigger events after ID is assigned
        savedOrder.afterCreate();

        return savedOrder.getOrderId();
    }

    /**
     * Add an item to an order
     */
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

    /**
     * Confirm an order
     */
    public void confirmOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        order.confirm();
        orderRepository.save(order);
    }

    /**
     * Cancel an order
     */
    public void cancelOrder(OrderId orderId, String reason) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        order.cancel(reason);
        orderRepository.save(order);
    }

    /**
     * Mark order payment as pending
     */
    public void markPaymentPending(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        order.markPaymentPending();
        orderRepository.save(order);
    }

    /**
     * Mark order as paid
     */
    public void markOrderPaid(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        order.markPaid();
        orderRepository.save(order);
    }

    /**
     * Mark order as refunded
     */
    public void markOrderRefunded(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        order.markRefunded();
        orderRepository.save(order);
    }

    /**
     * Get all orders for a customer
     */
    public List<Order> getOrdersByCustomer(CustomerId customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }
        return order;
    }
}