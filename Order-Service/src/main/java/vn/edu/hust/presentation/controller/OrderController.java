package vn.edu.hust.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hust.application.service.OrderApplicationService;
import vn.edu.hust.application.dto.query.OrderDTO;
import vn.edu.hust.application.dto.query.OrderSummaryDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @GetMapping("/v1/{orderId}/get")
    public CompletableFuture<ResponseEntity<OrderDTO>> getOrder(@PathVariable Long orderId) {
        return orderApplicationService.getOrder(orderId)
                .thenApply(order -> order != null ?
                        ResponseEntity.ok(order) :
                        ResponseEntity.notFound().build());
    }

    @PostMapping("/v1/{orderId}/apply-promotion")
    public CompletableFuture<ResponseEntity<Long>> applyPromotion(
            @PathVariable Long orderId,
            @RequestParam Long promotionId
    ) {
        return orderApplicationService.applyOrderPromotion(orderId, promotionId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(_ -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/v1/{orderId}/remove")
    public CompletableFuture<ResponseEntity<Void>> removeItemFromOrder(
            @PathVariable Long orderId,
            @RequestBody List<Long> ticketIdList) {
        return orderApplicationService.removeItemFromOrder(orderId, ticketIdList);
    }

    @PostMapping("/v1/{orderId}/confirm-and-pay")
    public CompletableFuture<ResponseEntity<String>> confirmOrder(@PathVariable Long orderId) {
        return orderApplicationService.confirmOrderAndInitiatePayment(orderId)
                .thenApply(_ -> ResponseEntity.ok("Order confirmed and payment initiated"))
                .exceptionally(throwable ->
                        ResponseEntity.badRequest().body("Failed to confirm order and initiate payment: " +
                                throwable.getMessage()));
    }

    @PostMapping("/v1/{orderId}/mark-paid")
    public CompletableFuture<ResponseEntity<Object>> markOrderAsPaid(
            @PathVariable Long orderId,
            @RequestParam Long paymentId) {
        return orderApplicationService.markOrderPaid(orderId, paymentId)
                .thenApply(_ -> ResponseEntity.ok().build())
                .exceptionally(_ -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/v1/customer/{customerId}")
    public CompletableFuture<ResponseEntity<List<OrderSummaryDTO>>> getOrdersByCustomer(@PathVariable Long customerId) {
        return orderApplicationService.getOrdersByCustomer(customerId)
                .thenApply(ResponseEntity::ok);
    }
}
