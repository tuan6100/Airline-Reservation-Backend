package vn.edu.hust.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hust.application.service.OrderApplicationService;
import vn.edu.hust.application.dto.command.CreateOrderCommand;
import vn.edu.hust.application.dto.query.OrderDTO;
import vn.edu.hust.application.dto.query.OrderSummaryDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @PostMapping
    public CompletableFuture<ResponseEntity<Long>> createOrder(@RequestBody CreateOrderCommand command) {
        return orderApplicationService.createOrder(command)
                .thenApply(orderId -> new ResponseEntity<>(orderId, HttpStatus.CREATED));
    }

    @GetMapping("/{orderId}")
    public CompletableFuture<ResponseEntity<OrderDTO>> getOrder(@PathVariable Long orderId) {
        return orderApplicationService.getOrder(orderId)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/{orderId}/confirm")
    public CompletableFuture<ResponseEntity<Void>> confirmOrder(@PathVariable Long orderId) {
        return orderApplicationService.confirmOrder(orderId)
                .thenApply(_ -> ResponseEntity.ok().build());
    }

    @PostMapping("/{orderId}/mark-paid")
    public CompletableFuture<ResponseEntity<Void>> markOrderAsPaid(
            @PathVariable Long orderId,
            @RequestParam Long paymentId) {
        return orderApplicationService.markOrderPaid(orderId, paymentId)
                .thenApply(_ -> ResponseEntity.ok().build());
    }

    @GetMapping("/customer/{customerId}")
    public CompletableFuture<ResponseEntity<List<OrderSummaryDTO>>> getOrdersByCustomer(@PathVariable Long customerId) {
        return orderApplicationService.getOrdersByCustomer(customerId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/booking/{bookingId}")
    public CompletableFuture<ResponseEntity<OrderDTO>> getOrderForBooking(@PathVariable String bookingId) {
        return orderApplicationService.getOrderByBooking(bookingId)
                .thenApply(order -> order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build());
    }
}
