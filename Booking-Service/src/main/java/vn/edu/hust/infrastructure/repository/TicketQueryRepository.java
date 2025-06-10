package vn.edu.hust.infrastructure.repository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hust.application.dto.query.TicketDTO;
import vn.edu.hust.infrastructure.entity.TicketEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class TicketQueryRepository {

    @Autowired
    private TicketJpaRepository ticketRepository;

    public TicketDTO findByTicketId(Long ticketId) {

        TicketEntity ticketEntity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        TicketDTO dto = new TicketDTO();
        dto.setTicketId(ticketEntity.getTicketId());
        dto.setTicketCode(ticketEntity.getTicketCode());
        dto.setFlightId(ticketEntity.getFlightId());
        dto.setFlightDepartureTime(ticketEntity.getFlightDepartureTime());
        dto.setSeatId(ticketEntity.getSeat().getSeatId());
        dto.setStatus(ticketEntity.getStatus());
        dto.setCreatedAt(ticketEntity.getCreatedAt());
        return dto;
    }

    public List<TicketDTO> findByFlightId(Long flightId) {
        return ticketRepository.findByFlightId(flightId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TicketDTO findAvailableByFlightId(Long flightId, LocalDateTime flightDepartureTime, Long seatId) {
        TicketEntity ticketEntity = ticketRepository.findAvailable(flightId, flightDepartureTime, seatId);
        return convertToDTO(ticketEntity);
    }

    private TicketDTO convertToDTO(TicketEntity ticketEntity) {
        TicketDTO dto = new TicketDTO();
        dto.setTicketId(ticketEntity.getTicketId());
        dto.setTicketCode(ticketEntity.getTicketCode());
        dto.setFlightId(ticketEntity.getFlightId());
        dto.setFlightDepartureTime(ticketEntity.getFlightDepartureTime());
        dto.setSeatId(ticketEntity.getSeat().getSeatId());
        dto.setStatus(ticketEntity.getStatus());
        dto.setCreatedAt(ticketEntity.getCreatedAt());
        return dto;
    }

}