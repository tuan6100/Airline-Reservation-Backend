package vn.edu.hust.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hust.application.service.SeatApplicationService;
import vn.edu.hust.application.dto.query.SeatDTO;
import vn.edu.hust.application.dto.command.HoldSeatCommand;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatApplicationService seatApplicationService;

    @GetMapping("/v2/flights/{flightId}/aircraft/{aircraftId}")
    public CompletableFuture<ResponseEntity<List<SeatDTO>>> getAvailableSeats(
            @PathVariable Long flightId,
            @PathVariable Long aircraftId) {
        return seatApplicationService.getAvailableSeatsByFlightAndAircraft(flightId, aircraftId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(_ -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/v2/{seatId}/hold")
    public CompletableFuture<ResponseEntity<String>> holdSeat(
            @PathVariable Long seatId,
            @RequestParam Long customerId,
            @RequestParam Long flightId
    ){
        return seatApplicationService.holdSeat(seatId, customerId, flightId)
                .thenApply(_ -> ResponseEntity.ok("Seat held successfully"))
                .exceptionally(ex -> ResponseEntity.badRequest()
                        .body("Failed to hold seat: " + ex.getMessage()));
    }

    @PostMapping("/v2/{seatId}/release")
    public CompletableFuture<ResponseEntity<String>> releaseSeat(@PathVariable Long seatId) {
        return seatApplicationService.releaseSeat(seatId)
                .thenApply(_ -> ResponseEntity.ok("Seat released successfully"))
                .exceptionally(ex -> ResponseEntity.badRequest()
                        .body("Failed to release seat: " + ex.getMessage()));
    }
}