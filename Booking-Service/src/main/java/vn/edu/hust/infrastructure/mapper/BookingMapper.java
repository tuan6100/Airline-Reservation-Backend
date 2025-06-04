package vn.edu.hust.infrastructure.mapper;

import org.springframework.stereotype.Component;
import vn.edu.hust.domain.model.aggregate.Booking;
import vn.edu.hust.infrastructure.entity.BookingEntity;

@Component
public class BookingMapper {

    public BookingEntity toEntity(Booking domain) {
        if (domain == null) {
            return null;
        }
        BookingEntity entity = new BookingEntity();
        entity.setBookingId(domain.getBookingId());
        entity.setCustomerId(domain.getCustomerId());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setSeatCount(domain.getSeatReservations().size());
        return entity;
    }
}