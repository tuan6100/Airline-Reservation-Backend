package vn.edu.hust.application.service;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.application.dto.command.HoldSeatCommand;
import vn.edu.hust.application.dto.command.ReleaseSeatCommand;
import vn.edu.hust.application.dto.query.SeatDTO;
import vn.edu.hust.domain.model.enumeration.SeatStatus;
import vn.edu.hust.infrastructure.repository.SeatJpaRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class SeatApplicationService {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private SeatJpaRepository seatRepository;

    public CompletableFuture<List<SeatDTO>> getAvailableSeatsByFlightAndAircraft(Long flightId, Long aircraftId) {
        return CompletableFuture.supplyAsync(() ->
                seatRepository.findByAircraftIdAndStatus(aircraftId, SeatStatus.AVAILABLE)
                .stream()
                .map(seat -> new SeatDTO(
                        seat.getSeatId(),
                        flightId,
                        seat.getSeatClassId(),
                        seat.getStatus()
                ))
                .collect(Collectors.toList()));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 30)
    public CompletableFuture<Void> holdSeat(Long seatId, Long customerId, Long flightId) {
        return CompletableFuture.runAsync(() -> {
            try {
                HoldSeatCommand command = new HoldSeatCommand();
                command.setSeatId(seatId);
                command.setCustomerId(customerId);
                command.setFlightId(flightId);
                boolean canHold = seatRepository.canHoldSeatAtomic(
                        command.getSeatId(),
                        SeatStatus.AVAILABLE,
                        command.getCustomerId()
                );
                if (!canHold) {
                    throw new IllegalStateException("Seat is not available for holding");
                }
                commandGateway.sendAndWait(command);
            } catch (Exception e) {
                throw new RuntimeException("Failed to hold seat: " + e.getMessage(), e);
            }
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 30)
    public CompletableFuture<Void> releaseSeat(Long seatId) {
        ReleaseSeatCommand command = new ReleaseSeatCommand();
        command.setSeatId(seatId);
        return commandGateway.send(command);
    }
}