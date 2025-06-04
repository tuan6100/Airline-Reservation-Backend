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
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingApplicationService bookingService;

    @PostMapping
    public CompletableFuture<ResponseEntity<String>> createBooking(@RequestBody CreateBookingCommand command) {
        return bookingService.createBooking(command)
                .thenApply(bookingId -> new ResponseEntity<>(bookingId, HttpStatus.CREATED));
    }

    @PostMapping("/{bookingId}/tickets")
    public CompletableFuture<ResponseEntity<Void>> addTicketToBooking(
            @PathVariable String bookingId,
            @RequestParam Long ticketId,
            @RequestParam Long seatId,
            @RequestParam Double price,
            @RequestParam(defaultValue = "VND") String currency) {
        return bookingService.addTicketToBooking(bookingId, ticketId, seatId, price, currency)
                .thenApply(result -> ResponseEntity.ok().build());
    }

    @DeleteMapping("/{bookingId}/tickets/{ticketId}")
    public CompletableFuture<ResponseEntity<Void>> removeTicketFromBooking(
            @PathVariable String bookingId,
            @PathVariable Long ticketId) {
        return bookingService.removeTicketFromBooking(bookingId, ticketId)
                .thenApply(result -> ResponseEntity.ok().build());
    }

    @PostMapping("/{bookingId}/expire")
    public CompletableFuture<ResponseEntity<Void>> expireBooking(@PathVariable String bookingId) {
        return bookingService.expireBooking(bookingId)
                .thenApply(result -> ResponseEntity.ok().build());
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
        return bookingService.cancelBooking(bookingId, reason).thenApply(result -> ResponseEntity.ok().build());
    }

    @GetMapping("/customer/{customerId}")
    public CompletableFuture<ResponseEntity<List<BookingDTO>>> getBookingsByCustomer(@PathVariable Long customerId) {
        return bookingService.getBookingsByCustomer(customerId)
                .thenApply(ResponseEntity::ok);
        }
    }
