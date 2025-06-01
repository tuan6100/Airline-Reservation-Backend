package vn.edu.hust.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hust.application.OrderApplicationService;
import vn.edu.hust.application.dto.command.CreateOrderCommand;
import vn.edu.hust.application.dto.command.UpdateOrderCommand;
import vn.edu.hust.application.dto.query.OrderDTO;
import vn.edu.hust.application.dto.query.OrderSummaryDTO;
import vn.edu.hust.infrastructure.dto.PaymentResponseDTO;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderCommand command) {
        OrderDTO order = orderApplicationService.createOrder(command);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId) {
        OrderDTO order = orderApplicationService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDTO> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderCommand command) {
        command.setOrderId(orderId);
        OrderDTO order = orderApplicationService.updateOrder(command);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderDTO> confirmOrder(@PathVariable Long orderId) {
        OrderDTO order = orderApplicationService.confirmOrder(orderId);
        return ResponseEntity.ok(order);
    }


    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false, defaultValue = "CUSTOMER_REQUEST") String reason) {
        OrderDTO order = orderApplicationService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/payment")
    public ResponseEntity<PaymentResponseDTO> processPayment(@PathVariable Long orderId) {
        PaymentResponseDTO response = orderApplicationService.processPayment(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/mark-paid")
    public ResponseEntity<OrderDTO> markOrderAsPaid(@PathVariable Long orderId) {
        OrderDTO order = orderApplicationService.markOrderAsPaid(orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<OrderDTO> markOrderAsRefunded(@PathVariable Long orderId) {
        OrderDTO order = orderApplicationService.markOrderAsRefunded(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderSummaryDTO>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<OrderSummaryDTO> orders = orderApplicationService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<OrderDTO> getOrderForBooking(@PathVariable Long bookingId) {
        OrderDTO order = orderApplicationService.getOrderForBooking(bookingId);
        if (order != null) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryDTO>> getAllOrders() {
        List<OrderSummaryDTO> orders = orderApplicationService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
}