package vn.edu.hust.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hust.application.dto.command.CreateTicketCommand;
import vn.edu.hust.application.dto.query.TicketDTO;
import vn.edu.hust.application.dto.query.TicketSummaryDTO;
import vn.edu.hust.application.service.TicketApplicationService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketApplicationService ticketApplicationService;

    @PostMapping
    public CompletableFuture<ResponseEntity<Long>> createTicket(@RequestBody CreateTicketCommand command) {
        return ticketApplicationService.createTicket(command)
                .thenApply(ticketId -> new ResponseEntity<>(ticketId, HttpStatus.CREATED));
    }

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

    @GetMapping("/flight/{flightId}/available")
    public CompletableFuture<ResponseEntity<List<TicketDTO>>> getAvailableTickets(@PathVariable Long flightId) {
        return ticketApplicationService.getAvailableTickets(flightId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/customer/{customerId}")
    public CompletableFuture<ResponseEntity<List<TicketSummaryDTO>>> getTicketsByCustomer(@PathVariable Long customerId) {
        return ticketApplicationService.getTicketsByCustomer(customerId)
                .thenApply(ResponseEntity::ok);
    }
}

