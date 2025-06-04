package vn.edu.hust.application.queryhandler;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.edu.hust.application.dto.query.BookingDTO;
import vn.edu.hust.application.dto.query.SeatDTO;
import vn.edu.hust.application.dto.query.GetAvailableSeatsQuery;
import vn.edu.hust.application.dto.query.GetBookingQuery;
import vn.edu.hust.application.dto.query.GetBookingsByCustomerQuery;
import vn.edu.hust.infrastructure.repository.BookingQueryRepository;
import vn.edu.hust.infrastructure.repository.SeatQueryRepository;

import java.util.List;

@Component
public class BookingQueryHandler {

    @Autowired
    private BookingQueryRepository bookingQueryRepository;

    @Autowired
    private SeatQueryRepository seatQueryRepository;

    @QueryHandler
    public BookingDTO handle(GetBookingQuery query) {
        return bookingQueryRepository.findByBookingId(query.getBookingId());
    }

    @QueryHandler
    public List<BookingDTO> handle(GetBookingsByCustomerQuery query) {
        return bookingQueryRepository.findByCustomerId(query.getCustomerId());
    }

    @QueryHandler
    public List<SeatDTO> handle(GetAvailableSeatsQuery query) {
        return seatQueryRepository.findAvailableByFlightId(query.getFlightId());
    }
}
