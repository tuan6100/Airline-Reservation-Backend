package vn.edu.hust.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hust.application.service.BookingApplicationService;
import vn.edu.hust.application.dto.command.CreateBookingCommand;
import vn.edu.hust.application.dto.query.BookingDTO;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired private BookingApplicationService bookingService;

    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody CreateBookingCommand command) {
        BookingDTO booking = bookingService.createBooking(command);
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDTO> getBooking(@PathVariable String bookingId) {
        BookingDTO booking = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingDTO> confirmBooking(@PathVariable String bookingId) {
        BookingDTO booking = bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingDTO> cancelBooking(
            @PathVariable String bookingId,
            @RequestParam String reason) {
        BookingDTO booking = bookingService.cancelBooking(bookingId, reason);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByCustomer(@PathVariable Long customerId) {
        List<BookingDTO> bookings = bookingService.getBookingsByCustomer(customerId);
        return ResponseEntity.ok(bookings);
    }
}
