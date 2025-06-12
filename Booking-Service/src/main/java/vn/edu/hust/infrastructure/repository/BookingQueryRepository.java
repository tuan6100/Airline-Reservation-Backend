package vn.edu.hust.infrastructure.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hust.application.dto.query.BookingDTO;
import vn.edu.hust.application.dto.query.TicketReservationDTO;
import vn.edu.hust.integration.rest.FlightClientService;
import vn.edu.hust.infrastructure.entity.BookingEntity;
import vn.edu.hust.infrastructure.entity.TicketEntity;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BookingQueryRepository {

    @Autowired
    private BookingJpaRepository bookingRepository;

    @Autowired
    private TicketJpaRepository ticketRepository;

    @Autowired
    private FlightClientService flightService;

    public BookingDTO findByBookingId(String bookingId) {
        BookingEntity bookingEntity = bookingRepository.findById(bookingId).orElse(null);
        if (bookingEntity == null) {
            return null;
        }
        BookingDTO dto = new BookingDTO();
        dto.setBookingId(bookingEntity.getBookingId());
        dto.setCustomerId(bookingEntity.getCustomerId());
        dto.setStatus(bookingEntity.getStatus());
        dto.setCreatedAt(bookingEntity.getCreatedAt());
        dto.setExpiresAt(bookingEntity.getExpiresAt());
        dto.setFlightId(bookingEntity.getFlightId());
        dto.setFlightDepartureTime(bookingEntity.getFlightDepartureTime());
        dto.setTotalAmount(bookingEntity.getTotalAmount());
        dto.setCurrency(bookingEntity.getCurrency());
        dto.setSeatCount(bookingEntity.getSeatCount());
        dto.setTicketCount(bookingEntity.getTicketCount());
        List<TicketEntity> tickets = ticketRepository.findByBookingId(bookingId);
        List<TicketReservationDTO> ticketReservations = tickets.stream()
                .map(this::convertToTicketReservationDTO)
                .collect(Collectors.toList());
        dto.setTickets(ticketReservations);
        if (bookingEntity.getFlightId() != null) {
            var flightDetails = flightService.getFlightDetails(bookingEntity.getFlightId());
            dto.setFlightDetails(flightDetails);
        }
        return dto;
    }

    public List<BookingDTO> findByCustomerId(Long customerId) {
        return bookingRepository.findByCustomerId(customerId)
                .stream()
                .map(bookingEntity -> {
                    BookingDTO dto = new BookingDTO();
                    dto.setBookingId(bookingEntity.getBookingId());
                    dto.setCustomerId(bookingEntity.getCustomerId());
                    dto.setStatus(bookingEntity.getStatus());
                    dto.setCreatedAt(bookingEntity.getCreatedAt());
                    dto.setExpiresAt(bookingEntity.getExpiresAt());
                    dto.setFlightId(bookingEntity.getFlightId());
                    dto.setFlightDepartureTime(bookingEntity.getFlightDepartureTime());
                    dto.setTotalAmount(bookingEntity.getTotalAmount());
                    dto.setCurrency(bookingEntity.getCurrency());
                    dto.setSeatCount(bookingEntity.getSeatCount());
                    dto.setTicketCount(bookingEntity.getTicketCount());
                    List<TicketEntity> tickets = ticketRepository.findByBookingId(bookingEntity.getBookingId());
                    List<TicketReservationDTO> ticketReservations = tickets.stream()
                            .map(this::convertToTicketReservationDTO)
                            .collect(Collectors.toList());
                    dto.setTickets(ticketReservations);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    private TicketReservationDTO convertToTicketReservationDTO(TicketEntity ticket) {
        TicketReservationDTO dto = new TicketReservationDTO();
        dto.setTicketId(ticket.getTicketId());
        dto.setSeatId(ticket.getSeatId());
        if (ticket.getSeat() != null) {
            dto.setSeatCode(ticket.getSeat().getSeatCode());
            if (ticket.getSeat().getSeatClass() != null) {
                dto.setSeatClassName(ticket.getSeat().getSeatClass().getSeatClassName());
            }
        }
        return dto;
    }
}