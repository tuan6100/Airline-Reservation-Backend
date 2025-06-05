package vn.edu.hust.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hust.application.service.BookingApplicationService;
import vn.edu.hust.application.dto.command.CreateBookingCommand;
import vn.edu.hust.application.dto.query.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingApplicationService bookingService;

    @GetMapping("/flights/{flightId}/tickets/availability")
    public CompletableFuture<ResponseEntity<TicketAvailabilityDTO>> getTicketAvailability(@PathVariable Long flightId) {
        return bookingService.searchAvailableTickets(flightId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/flights/{flightId}/tickets")
    public CompletableFuture<ResponseEntity<List<TicketSearchDTO>>> searchTickets(
            @PathVariable Long flightId,
            @RequestParam(required = false) Long seatClassId) {
        return bookingService.searchTicketsBySeatClass(flightId, seatClassId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/with-tickets")
    public CompletableFuture<ResponseEntity<String>> createBookingWithTickets(@RequestBody CreateBookingCommand command) {
        return bookingService.createBookingWithTickets(command)
                .thenApply(bookingId -> new ResponseEntity<>(bookingId, HttpStatus.CREATED))
                .exceptionally(ex -> {
                    return new ResponseEntity<>("Failed to create booking: " + ex.getMessage(),
                            HttpStatus.BAD_REQUEST);
                });
    }

    @PostMapping("/{bookingId}/confirm-and-order")
    public CompletableFuture<ResponseEntity<String>> confirmBookingAndCreateOrder(@PathVariable String bookingId) {
        return bookingService.confirmBookingAndCreateOrder(bookingId)
                .thenApply(result -> ResponseEntity.ok("Booking confirmed and order creation triggered"))
                .exceptionally(ex -> ResponseEntity.badRequest()
                        .body("Failed to confirm booking: " + ex.getMessage()));
    }

    @GetMapping("/{bookingId}")
    public CompletableFuture<ResponseEntity<BookingDTO>> getBooking(@PathVariable String bookingId) {
        return bookingService.getBooking(bookingId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/customer/{customerId}")
    public CompletableFuture<ResponseEntity<List<BookingDTO>>> getBookingsByCustomer(@PathVariable Long customerId) {
        return bookingService.getBookingsByCustomer(customerId)
                .thenApply(ResponseEntity::ok);
    }
}