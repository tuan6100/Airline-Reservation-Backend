package vn.edu.hust.infrastructure.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.edu.hust.application.dto.query.TicketBookedDTO;
import vn.edu.hust.domain.event.TicketBookedEvent;
import vn.edu.hust.infrastructure.entity.TicketEntity;
import vn.edu.hust.infrastructure.repository.TicketJpaRepository;
import vn.edu.hust.infrastructure.repository.SeatClassJpaRepository;

@Component
public class TicketMapper {

    @Autowired
    private TicketJpaRepository ticketRepository;

    @Autowired
    private SeatClassJpaRepository seatClassRepository;


    public TicketBookedDTO fromEventToDTO(TicketBookedEvent event) {
        try {
            TicketEntity ticket = ticketRepository.findById(event.ticketId())
                    .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + event.ticketId()));
            TicketBookedDTO dto = new TicketBookedDTO();
            dto.setTicketId(event.ticketId());
            dto.setTicketCode(ticket.getTicketCode());
            dto.setStatus(ticket.getStatus());
            dto.setCreatedAt(ticket.getCreatedAt());
            dto.setBookingId(event.bookingId());
            dto.setSeatId(event.seatId() != null ? event.seatId() : ticket.getSeatId());
            if (ticket.getSeat() != null) {
                dto.setSeatCode(ticket.getSeat().getSeatCode());
                if (ticket.getSeat().getSeatClass() != null) {
                    dto.setPrice(ticket.getSeat().getSeatClass().getPrice().longValue());
                } else {
                    Long seatClassId = ticket.getSeat().getSeatClassId();
                    if (seatClassId != null) {
                        dto.setPrice(seatClassRepository.findById(seatClassId.intValue())
                                .map(sc -> sc.getPrice().longValue())
                                .orElse(getDefaultPrice(seatClassId)));
                    }
                }
            }
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map TicketBookedEvent to TicketBookedDTO: " + e.getMessage(), e);
        }
    }

    public TicketBookedDTO fromEntityToDTO(TicketEntity ticket) {
        TicketBookedDTO dto = new TicketBookedDTO();
        dto.setTicketId(ticket.getTicketId());
        dto.setTicketCode(ticket.getTicketCode());
        dto.setStatus(ticket.getStatus());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setBookingId(ticket.getBookingId());
        dto.setSeatId(ticket.getSeatId());
        if (ticket.getSeat() != null) {
            dto.setSeatCode(ticket.getSeat().getSeatCode());
            if (ticket.getSeat().getSeatClass() != null) {
                dto.setPrice(ticket.getSeat().getSeatClass().getPrice().longValue());
            }
        }
        return dto;
    }


    private Long getDefaultPrice(Long seatClassId) {
        return switch (seatClassId.intValue()) {
            case 2 -> 3000000L;
            case 3 -> 5000000L;
            default -> 1000000L;
        };
    }
}