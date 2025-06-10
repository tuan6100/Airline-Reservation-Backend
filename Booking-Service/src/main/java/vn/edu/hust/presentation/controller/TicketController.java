package vn.edu.hust.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hust.application.dto.command.CreateTicketCommand;
import vn.edu.hust.application.dto.query.GetAvailableTicketsQuery;
import vn.edu.hust.application.dto.query.TicketDTO;
import vn.edu.hust.application.service.TicketApplicationService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketApplicationService ticketApplicationService;

    @GetMapping("/{ticketId}")
    public CompletableFuture<ResponseEntity<TicketDTO>> getTicket(@PathVariable Long ticketId) {
        return ticketApplicationService.getTicket(ticketId)
                .thenApply(ticket -> ticket != null ? ResponseEntity.ok(ticket) : ResponseEntity.notFound().build());
    }

    @PostMapping("/{ticketId}/hold")
    public CompletableFuture<ResponseEntity<Void>> holdTicket(
            @PathVariable Long ticketId,
            @RequestParam Long customerId,
            @RequestParam(required = false, defaultValue = "15") Integer holdDurationMinutes) {
        return ticketApplicationService.holdTicket(ticketId, customerId, holdDurationMinutes)
                .thenApply(_ -> ResponseEntity.ok().build());
    }

    @PostMapping("/{ticketId}/book")
    public CompletableFuture<ResponseEntity<Void>> bookTicket(
            @PathVariable Long ticketId,
            @RequestParam Long customerId,
            @RequestParam String bookingId) {
        return ticketApplicationService.bookTicket(ticketId, customerId, bookingId)
                .thenApply(_ -> ResponseEntity.ok().build());
    }

    @PostMapping("/{ticketId}/release")
    public CompletableFuture<ResponseEntity<Void>> releaseTicket(@PathVariable Long ticketId) {
        return ticketApplicationService.releaseTicket(ticketId)
                .thenApply(_ -> ResponseEntity.ok().build());
    }

    @PostMapping("/{ticketId}/cancel")
    public CompletableFuture<ResponseEntity<Void>> cancelTicket(
            @PathVariable Long ticketId,
            @RequestParam String reason) {
        return ticketApplicationService.cancelTicket(ticketId, reason)
                .thenApply(_ -> ResponseEntity.ok().build());
    }

    @GetMapping("/flight/{flightId}")
    public CompletableFuture<ResponseEntity<List<TicketDTO>>> getTicketsByFlight(@PathVariable Long flightId) {
        return ticketApplicationService.getTicketsByFlight(flightId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/flight/available")
    public CompletableFuture<ResponseEntity<TicketDTO>> getAvailableTickets(
            @RequestBody GetAvailableTicketsQuery availableTicketsQuery) {
        return ticketApplicationService.getAvailableTickets(
                availableTicketsQuery.getFlightId(),
                availableTicketsQuery.getFlightDepartureTime(),
                availableTicketsQuery.getSeatId()
                ).thenApply(ResponseEntity::ok);
    }
}

