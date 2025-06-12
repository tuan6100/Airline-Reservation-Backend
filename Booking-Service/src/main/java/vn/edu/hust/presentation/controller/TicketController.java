package vn.edu.hust.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hust.application.dto.query.SeatAndFlightDTO;
import vn.edu.hust.application.dto.query.TicketDTO;
import vn.edu.hust.application.service.TicketApplicationService;
import vn.edu.hust.application.dto.command.BookTicketCommand;
import vn.edu.hust.presentation.payload.SeatAndFlightRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketApplicationService ticketApplicationService;

    @GetMapping("/v2/search")
    public CompletableFuture<ResponseEntity<?>> getTicketsBySingleSeatAndSingleFlight(
            @RequestBody List<SeatAndFlightRequest> requests
    ) {
        List<SeatAndFlightDTO> seatAndFlightDTOList = new ArrayList<>();
        requests.forEach(request -> request.seatIds().forEach(seatId -> {
            SeatAndFlightDTO seatAndFlightDTO = new SeatAndFlightDTO(
                    seatId, request.flightId(), request.flightDepartureTime()
            );
            seatAndFlightDTOList.add(seatAndFlightDTO);
        }));
        return ticketApplicationService.getTicketsBySeatAndFlight(seatAndFlightDTOList)
                .thenApply(tickets -> {
                    if (tickets.isEmpty()) {
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok(tickets);
                })
                .exceptionally(_ -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/v1/{ticketId}")
    public CompletableFuture<ResponseEntity<TicketDTO>> getTicket(@PathVariable Long ticketId) {
        return ticketApplicationService.getTicket(ticketId)
                .thenApply(ticket -> ticket != null ? ResponseEntity.ok(ticket) : ResponseEntity.notFound().build());
    }

    @PostMapping("/v2/{ticketId}/book")
    public CompletableFuture<ResponseEntity<String>> bookTicket(
            @PathVariable Long ticketId,
            @RequestParam Long customerId,
            @RequestParam String bookingId) {
        BookTicketCommand command = new BookTicketCommand();
        command.setTicketId(ticketId);
        command.setCustomerId(customerId);
        command.setBookingId(bookingId);
        return ticketApplicationService.bookTicket(command)
                .thenApply(_ -> ResponseEntity.ok("Ticket booked successfully"))
                .exceptionally(ex -> ResponseEntity.badRequest()
                        .body("Failed to book ticket: " + ex.getMessage()));
    }

    @PostMapping("/v2/{ticketId}/hold")
    public CompletableFuture<ResponseEntity<String>> holdTicket(
            @PathVariable Long ticketId,
            @RequestParam Long customerId) {
        return ticketApplicationService.holdTicket(ticketId, customerId)
                .thenApply(_ -> ResponseEntity.ok("Ticket held successfully"))
                .exceptionally(ex -> ResponseEntity.badRequest()
                        .body("Failed to hold ticket: " + ex.getMessage()));
    }

    @PostMapping("/v2/{ticketId}/release")
    public CompletableFuture<ResponseEntity<String>> releaseTicket(@PathVariable Long ticketId) {
        return ticketApplicationService.releaseTicket(ticketId)
                .thenApply(_ -> ResponseEntity.ok("Ticket released successfully"))
                .exceptionally(ex -> ResponseEntity.badRequest()
                        .body("Failed to release ticket: " + ex.getMessage()));
    }

    @PostMapping("/v2/{ticketId}/cancel")
    public CompletableFuture<ResponseEntity<String>> cancelTicket(
            @PathVariable Long ticketId,
            @RequestParam String reason) {
        return ticketApplicationService.cancelTicket(ticketId, reason)
                .thenApply(_ -> ResponseEntity.ok("Ticket cancelled successfully"))
                .exceptionally(ex -> ResponseEntity.badRequest()
                        .body("Failed to cancel ticket: " + ex.getMessage()));
    }

    @GetMapping("/v2/flight/{flightId}")
    public CompletableFuture<ResponseEntity<List<TicketDTO>>> getTicketsByFlight(@PathVariable Long flightId) {
        return ticketApplicationService.getTicketsByFlight(flightId)
                .thenApply(ResponseEntity::ok);
    }
}