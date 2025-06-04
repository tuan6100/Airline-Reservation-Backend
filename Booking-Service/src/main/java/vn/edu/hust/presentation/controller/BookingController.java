package vn.edu.hust.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hust.application.service.BookingApplicationService;
import vn.edu.hust.application.dto.command.CreateBookingCommand;
import vn.edu.hust.application.dto.query.BookingDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired private BookingApplicationService bookingService;

    @PostMapping
    public CompletableFuture<ResponseEntity<String>> createBooking(@RequestBody CreateBookingCommand command) {
        return bookingService.createBooking(command)
                .thenApply(bookingId -> new ResponseEntity<>(bookingId, HttpStatus.CREATED));
    }

    @GetMapping("/{bookingId}")
    public CompletableFuture<ResponseEntity<BookingDTO>> getBooking(@PathVariable String bookingId) {
        return bookingService.getBooking(bookingId)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/{bookingId}/confirm")
    public CompletableFuture<ResponseEntity<Void>> confirmBooking(@PathVariable String bookingId) {
        return bookingService.confirmBooking(bookingId)
                .thenApply(result -> ResponseEntity.ok().build());
    }

    @PostMapping("/{bookingId}/cancel")
    public CompletableFuture<ResponseEntity<Void>> cancelBooking(
            @PathVariable String bookingId,
            @RequestParam String reason) {
        return bookingService.cancelBooking(bookingId, reason)
                .thenApply(result -> ResponseEntity.ok().build());
    }

    @GetMapping("/customer/{customerId}")
    public CompletableFuture<ResponseEntity<List<BookingDTO>>> getBookingsByCustomer(@PathVariable Long customerId) {
        return bookingService.getBookingsByCustomer(customerId)
                .thenApply(ResponseEntity::ok);
    }
}