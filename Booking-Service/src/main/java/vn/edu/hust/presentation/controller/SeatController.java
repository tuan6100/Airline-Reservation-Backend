package vn.edu.hust.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hust.application.service.BookingApplicationService;
import vn.edu.hust.application.dto.query.SeatDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/flights/{flightId}/seats")
public class SeatController {

    @Autowired private BookingApplicationService bookingService;

    @GetMapping("/available")
    public CompletableFuture<ResponseEntity<List<SeatDTO>>> getAvailableSeats(@PathVariable Long flightId) {
        return bookingService.getAvailableSeats(flightId).thenApply(ResponseEntity::ok);
    }
}
