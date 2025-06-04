package vn.edu.hust.infrastructure.mapper;

import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.aggregate.Booking;
import vn.edu.hust.domain.model.valueobj.SeatReservation;
import vn.edu.hust.infrastructure.entity.BookingEntity;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookingMapper {


    public Booking toDomain(BookingEntity entity) {
        Booking booking = new Booking();
        booking.setBookingId(entity.getBookingId());
        booking.setCustomerId(entity.getCustomerId());
        booking.setStatus((entity.getStatus()));
        booking.setCreatedAt(entity.getCreatedAt());
        booking.setExpiresAt(entity.getExpiresAt());

        Set<SeatReservation> seatReservations =
        booking.setSeatReservations(seatReservations);

        return booking;
    }

    public BookingEntity toEntity(Booking domain) {
        BookingEntity entity = new BookingEntity();
        entity.setBookingId(domain.getBookingId());
        entity.setCustomerId(domain.getCustomerId());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiresAt(domain.getExpiresAt());

        return entity;
    }
}
