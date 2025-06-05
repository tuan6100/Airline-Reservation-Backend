package vn.edu.hust.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.application.dto.query.TicketSearchDTO;
import vn.edu.hust.application.dto.query.TicketAvailabilityDTO;
import vn.edu.hust.infrastructure.entity.TicketEntity;
import vn.edu.hust.infrastructure.repository.TicketJpaRepository;
import vn.edu.hust.infrastructure.repository.SeatJpaRepository;
import vn.edu.hust.infrastructure.repository.SeatClassJpaRepository;
import vn.edu.hust.infrastructure.entity.SeatEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TicketSearchService {

    @Autowired
    private TicketJpaRepository ticketRepository;

    @Autowired
    private SeatJpaRepository seatRepository;

    @Autowired
    private SeatClassJpaRepository seatClassRepository;

    public List<TicketSearchDTO> searchAvailableTickets(Long flightId, Long seatClassId) {
        List<TicketEntity> tickets = ticketRepository.findAvailableTicketsByFlightAndSeatClass(flightId, seatClassId);

        return tickets.stream()
                .map(this::convertToSearchDTO)
                .collect(Collectors.toList());
    }

    public Optional<TicketSearchDTO> findTicketForSeat(Long flightId, Long seatId, LocalDateTime departureTime) {
        Optional<TicketEntity> ticketOpt = ticketRepository.findAvailableTicketByFlightAndSeat(
                flightId, seatId, departureTime
        );

        return ticketOpt.map(this::convertToSearchDTO);
    }

    public TicketAvailabilityDTO getFlightAvailability(Long flightId) {
        TicketAvailabilityDTO availability = new TicketAvailabilityDTO();
        availability.setFlightId(flightId);
        List<TicketEntity> economyTickets = ticketRepository.findAvailableTicketsByFlightAndSeatClass(flightId, 1L);
        availability.setEconomyCount(economyTickets.size());
        availability.setEconomyTickets(economyTickets.stream().map(this::convertToSearchDTO).collect(Collectors.toList()));
        List<TicketEntity> businessTickets = ticketRepository.findAvailableTicketsByFlightAndSeatClass(flightId, 2L);
        availability.setBusinessCount(businessTickets.size());
        availability.setBusinessTickets(businessTickets.stream().map(this::convertToSearchDTO).collect(Collectors.toList()));
        List<TicketEntity> firstClassTickets = ticketRepository.findAvailableTicketsByFlightAndSeatClass(flightId, 3L);
        availability.setFirstClassCount(firstClassTickets.size());
        availability.setFirstClassTickets(firstClassTickets.stream().map(this::convertToSearchDTO).collect(Collectors.toList()));

        return availability;
    }

    private TicketSearchDTO convertToSearchDTO(TicketEntity ticket) {
        TicketSearchDTO dto = new TicketSearchDTO();
        dto.setTicketId(ticket.getTicketId());
        dto.setTicketCode(ticket.getTicketCode());
        dto.setFlightId(ticket.getFlightId());
        dto.setFlightDepartureTime(ticket.getFlightDepartureTime());
        dto.setStatus(ticket.getStatus());
        dto.setCreatedAt(ticket.getCreatedAt());

        if (ticket.getSeat() != null) {
            SeatEntity seat = ticket.getSeat();
            dto.setSeatId(seat.getSeatId());
            dto.setSeatCode(seat.getSeatCode());
            dto.setSeatClassId(seat.getSeatClassId());
            if (seat.getSeatClass() != null) {
                dto.setSeatClassName(seat.getSeatClass().getSeatClassName());
                dto.setPrice(seat.getSeatClass().getPrice().doubleValue());
                dto.setCurrency("VND");
            } else {
                dto.setPrice(getPriceFromSeatClass(seat.getSeatClassId()));
                dto.setCurrency("VND");
            }
        }

        return dto;
    }

    private Double getPriceFromSeatClass(Long seatClassId) {
        try {
            return seatClassRepository.findById(seatClassId.intValue())
                    .map(seatClass -> seatClass.getPrice().doubleValue())
                    .orElse(getDefaultPrice(seatClassId));
        } catch (Exception e) {
            return getDefaultPrice(seatClassId);
        }
    }

    private Double getDefaultPrice(Long seatClassId) {
        return switch (seatClassId.intValue()) {
            case 2 -> 3000000.0;
            case 3 -> 5000000.0;
            default -> 1000000.0;
        };
    }
}