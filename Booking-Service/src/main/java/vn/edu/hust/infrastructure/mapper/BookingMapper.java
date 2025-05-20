package vn.edu.hust.infrastructure.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.aggregate.Booking;
import vn.edu.hust.domain.model.enumeration.BookingStatus;
import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.CustomerId;
import vn.edu.hust.domain.model.valueobj.SeatReservation;
import vn.edu.hust.infrastructure.entity.BookingEntity;
import vn.edu.hust.infrastructure.entity.SeatReservationEntity;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookingMapper {
    private final SeatReservationMapper seatReservationMapper;

    @Autowired
    public BookingMapper(SeatReservationMapper seatReservationMapper) {
        this.seatReservationMapper = seatReservationMapper;
    }

    public Booking toDomain(BookingEntity entity) {
        Booking booking = new Booking();
        booking.setBookingId(new BookingId(entity.getId()));
        booking.setCustomerId(new CustomerId(entity.getCustomerId()));
        booking.setStatus(BookingStatus.valueOf(entity.getStatus()));
        booking.setCreatedAt(entity.getCreatedAt());
        booking.setExpiresAt(entity.getExpiresAt());

        Set<SeatReservation> seatReservations = entity.getSeatReservations().stream()
                .map(seatReservationMapper::toDomain)
                .collect(Collectors.toSet());
        booking.setSeatReservations(seatReservations);

        return booking;
    }

    public BookingEntity toEntity(Booking domain) {
        BookingEntity entity = new BookingEntity();
        entity.setId(domain.getBookingId().value());
        entity.setCustomerId(domain.getCustomerId().value());
        entity.setStatus(domain.getStatus().name());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiresAt(domain.getExpiresAt());

        Set<SeatReservationEntity> seatReservations = domain.getSeatReservations().stream()
                .map(seatReservationMapper::toEntity)
                .collect(Collectors.toSet());
        entity.setSeatReservations(seatReservations);

        return entity;
    }
}
