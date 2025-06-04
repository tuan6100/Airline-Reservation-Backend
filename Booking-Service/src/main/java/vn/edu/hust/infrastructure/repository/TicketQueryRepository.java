package vn.edu.hust.infrastructure.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hust.application.dto.query.TicketDTO;
import vn.edu.hust.application.dto.query.TicketSummaryDTO;
import vn.edu.hust.application.service.FlightService;
import vn.edu.hust.infrastructure.entity.SeatEntity;
import vn.edu.hust.infrastructure.entity.TicketEntity;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class TicketQueryRepository {

    @Autowired
    private TicketJpaRepository ticketRepository;

    @Autowired
    private SeatJpaRepository seatRepository;

    @Autowired
    private FlightService flightService;

    public TicketDTO findByTicketId(Long ticketId) {
        TicketEntity ticketEntity = ticketRepository.findById(ticketId).get();
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

    public List<TicketDTO> findAvailableByFlightId(Long flightId) {
        return ticketRepository.findAvailableByFlightId(flightId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

    private TicketSummaryDTO convertToSummaryDTO(TicketEntity ticketEntity) {
        TicketSummaryDTO dto = new TicketSummaryDTO();
        dto.setTicketId(ticketEntity.getTicketId());
        dto.setTicketCode(ticketEntity.getTicketCode());
        dto.setFlightId(ticketEntity.getFlightId());
        dto.setSeatId(ticketEntity.getSeat().getSeatId());
        dto.setStatus(ticketEntity.getStatus());
        dto.setCreatedAt(ticketEntity.getCreatedAt());
        dto.setDepartureTime(ticketEntity.getFlightDepartureTime());
        SeatEntity seatEntity = seatRepository.findById(ticketEntity.getSeat().getSeatId()).get();
        dto.setSeatCode(seatEntity.getSeatCode());
        try {
            var flightDetails = flightService.getFlightDetails(
                    new vn.edu.hust.domain.model.valueobj.FlightId(ticketEntity.getFlightId())
            );
            if (flightDetails != null) {
                dto.setFlightNumber(flightDetails.getFlightNumber());
                dto.setDepartureAirport(flightDetails.getDepartureAirportCode());
                dto.setArrivalAirport(flightDetails.getArrivalAirportCode());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return dto;
    }

}