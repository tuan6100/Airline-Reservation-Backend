package vn.edu.hust.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.application.dto.command.CreateOrderCommand;
import vn.edu.hust.application.dto.command.UpdateOrderCommand;
import vn.edu.hust.application.dto.query.OrderDTO;
import vn.edu.hust.application.dto.query.OrderItemDTO;
import vn.edu.hust.application.dto.query.OrderSummaryDTO;
import vn.edu.hust.domain.exception.OrderNotFoundException;
import vn.edu.hust.domain.model.aggregate.Order;
import vn.edu.hust.domain.model.entity.OrderItem;
import vn.edu.hust.domain.model.valueobj.*;
import vn.edu.hust.domain.repository.OrderRepository;
import vn.edu.hust.domain.service.OrderDomainService;
import vn.edu.hust.infrastructure.dto.PaymentResponseDTO;
import vn.edu.hust.infrastructure.service.BookingServiceClient;
import vn.edu.hust.infrastructure.service.PaymentServiceClient;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service for Order operations
 */
@Service
public class OrderApplicationService {

    @Autowired
    private OrderDomainService orderDomainService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookingServiceClient bookingServiceClient;

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    /**
     * Create a new order
     */
    @Transactional
    public OrderDTO createOrder(CreateOrderCommand command) {
        BookingId bookingId = new BookingId(command.getBookingId());
        CustomerId customerId = new CustomerId(command.getCustomerId());
        PromotionId promotionId = command.getPromotionId() != null ?
                new PromotionId(command.getPromotionId()) : null;

        OrderId orderId = orderDomainService.createOrder(bookingId, customerId, promotionId);

        // Add items to order
        command.getItems().forEach(item -> {
            orderDomainService.addOrderItem(
                    orderId,
                    new TicketId(item.getTicketId()),
                    new FlightId(item.getFlightId()),
                    new SeatId(item.getSeatId()),
                    new Money(item.getPrice(), Currency.getInstance(item.getCurrency())),
                    item.getDescription()
            );
        });

        Order order = orderRepository.findById(orderId);
        return convertToDTO(order);
    }

    /**
     * Confirm an order
     */
    @Transactional
    public OrderDTO confirmOrder(Long orderId) {
        orderDomainService.confirmOrder(new OrderId(orderId));

        Order order = orderRepository.findById(new OrderId(orderId));

        // Notify Booking Service that order is confirmed
        bookingServiceClient.confirmBooking(order.getBookingId());

        return convertToDTO(order);
    }

    /**
     * Cancel an order
     */
    @Transactional
    public OrderDTO cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(new OrderId(orderId));
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        orderDomainService.cancelOrder(new OrderId(orderId), reason);

        // Notify Booking Service that order is cancelled
        bookingServiceClient.cancelBooking(order.getBookingId(), reason);

        order = orderRepository.findById(new OrderId(orderId));
        return convertToDTO(order);
    }

    /**
     * Process payment for an order
     */
    @Transactional
    public PaymentResponseDTO processPayment(Long orderId) {
        Order order = orderRepository.findById(new OrderId(orderId));
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        // Mark order as payment pending
        orderDomainService.markPaymentPending(new OrderId(orderId));

        // Initiate payment
        PaymentResponseDTO response = paymentServiceClient.initiatePayment(
                new OrderId(orderId),
                order.getTotalAmount().getAmount(),
                order.getTotalAmount().getCurrency().getCurrencyCode(),
                order.getCustomerId().value()
        );

        return response;
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long orderId) {
        Order order = orderRepository.findById(new OrderId(orderId));
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        return convertToDTO(order);
    }

    /**
     * Get orders by customer ID
     */
    @Transactional(readOnly = true)
    public List<OrderSummaryDTO> getOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(new CustomerId(customerId));

        return orders.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get order for booking
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderForBooking(Long bookingId) {
        Order order = orderRepository.findByBookingId(new BookingId(bookingId));
        if (order == null) {
            return null;
        }
        return convertToDTO(order);
    }

    /**
     * Update an order
     */
    @Transactional
    public OrderDTO updateOrder(UpdateOrderCommand command) {
        Order order = orderRepository.findById(new OrderId(command.getOrderId()));
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + command.getOrderId());
        }

        // Update order properties if needed
        // This is a simplified implementation - actual update logic would depend on business requirements

        order = orderRepository.save(order);
        return convertToDTO(order);
    }

    /**
     * Mark order as paid
     */
    @Transactional
    public OrderDTO markOrderAsPaid(Long orderId) {
        orderDomainService.markOrderPaid(new OrderId(orderId));
        Order order = orderRepository.findById(new OrderId(orderId));
        return convertToDTO(order);
    }

    /**
     * Mark order as refunded
     */
    @Transactional
    public OrderDTO markOrderAsRefunded(Long orderId) {
        orderDomainService.markOrderRefunded(new OrderId(orderId));
        Order order = orderRepository.findById(new OrderId(orderId));
        return convertToDTO(order);
    }

    /**
     * Get all orders
     */
    @Transactional(readOnly = true)
    public List<OrderSummaryDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Order domain model to OrderDTO
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId().value());
        dto.setCustomerId(order.getCustomerId().value());
        dto.setBookingId(order.getBookingId().value());
        if (order.getPromotionId() != null) {
            dto.setPromotionId(order.getPromotionId().value());
        }
        dto.setStatus(order.getStatus().name());
        dto.setPaymentStatus(order.getPaymentStatus().name());
        dto.setTotalAmount(order.getTotalAmount().getAmount());
        dto.setCurrency(order.getTotalAmount().getCurrency().getCurrencyCode());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Convert order items
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    /**
     * Convert Order domain model to OrderSummaryDTO
     */
    private OrderSummaryDTO convertToSummaryDTO(Order order) {
        OrderSummaryDTO dto = new OrderSummaryDTO();
        dto.setOrderId(order.getOrderId().value());
        dto.setCustomerId(order.getCustomerId().value());
        dto.setBookingId(order.getBookingId().value());
        dto.setStatus(order.getStatus().name());
        dto.setPaymentStatus(order.getPaymentStatus().name());
        dto.setTotalAmount(order.getTotalAmount().getAmount());
        dto.setCurrency(order.getTotalAmount().getCurrency().getCurrencyCode());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setItemCount(order.getOrderItems().size());
        return dto;
    }

    /**
     * Convert OrderItem domain model to OrderItemDTO
     */
    private OrderItemDTO convertToItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setTicketId(item.getTicketId().value());
        dto.setFlightId(item.getFlightId().value());
        dto.setSeatId(item.getSeatId().value());
        dto.setPrice(item.getPrice().getAmount());
        dto.setCurrency(item.getPrice().getCurrency().getCurrencyCode());
        dto.setDescription(item.getDescription());
        return dto;
    }
}
