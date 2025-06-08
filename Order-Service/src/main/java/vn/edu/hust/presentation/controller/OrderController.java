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
                .thenApply(orderId -> new ResponseEntity<>(orderId, HttpStatus.CREATED))
                .exceptionally(_ -> new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));
    }

    @GetMapping("/{orderId}")
    public CompletableFuture<ResponseEntity<OrderDTO>> getOrder(@PathVariable Long orderId) {
        return orderApplicationService.getOrder(orderId)
                .thenApply(order -> order != null ?
                        ResponseEntity.ok(order) :
                        ResponseEntity.notFound().build());
    }

    @PostMapping("/{orderId}/confirm")
    public CompletableFuture<ResponseEntity<Object>> confirmOrder(@PathVariable Long orderId) {
        return orderApplicationService.confirmOrder(orderId)
                .thenApply(_ -> ResponseEntity.ok().build())
                .exceptionally(_ -> ResponseEntity.badRequest().build());
    }


    @PostMapping("/{orderId}/confirm-and-pay")
    public CompletableFuture<ResponseEntity<String>> confirmOrderAndInitiatePayment(@PathVariable Long orderId) {
        return orderApplicationService.confirmOrderAndInitiatePayment(orderId)
                .thenApply(_ -> ResponseEntity.ok("Order confirmed and payment initiated"))
                .exceptionally(throwable ->
                        ResponseEntity.badRequest().body("Failed to confirm order and initiate payment: " +
                                throwable.getMessage()));
    }

    @PostMapping("/{orderId}/mark-paid")
    public CompletableFuture<ResponseEntity<Object>> markOrderAsPaid(
            @PathVariable Long orderId,
            @RequestParam Long paymentId) {
        return orderApplicationService.markOrderPaid(orderId, paymentId)
                .thenApply(_ -> ResponseEntity.ok().build())
                .exceptionally(_ -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/customer/{customerId}")
    public CompletableFuture<ResponseEntity<List<OrderSummaryDTO>>> getOrdersByCustomer(@PathVariable Long customerId) {
        return orderApplicationService.getOrdersByCustomer(customerId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/booking/{bookingId}")
    public CompletableFuture<ResponseEntity<OrderDTO>> getOrderForBooking(@PathVariable String bookingId) {
        return orderApplicationService.getOrderByBooking(bookingId)
                .thenApply(order -> order != null ?
                        ResponseEntity.ok(order) :
                        ResponseEntity.notFound().build());
    }
}
