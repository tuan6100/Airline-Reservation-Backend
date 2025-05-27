package vn.edu.hust.infrastructure.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.aggregate.Booking;
import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.CustomerId;
import vn.edu.hust.domain.repository.BookingRepository;
import vn.edu.hust.infrastructure.entity.BookingEntity;
import vn.edu.hust.infrastructure.mapper.BookingMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DomainBookingRepository implements BookingRepository {

    @Autowired private BookingJpaRepository bookingJpaRepository;
    @Autowired private BookingMapper bookingMapper;


    @Override
    public Booking findById(BookingId bookingId) {
        return bookingJpaRepository.findById(bookingId.value())
                .map(bookingMapper::toDomain)
                .orElse(null);
    }

    @Override
    public void save(Booking booking) {
        BookingEntity entity = bookingMapper.toEntity(booking);
        bookingJpaRepository.save(entity);
    }

    @Override
    public List<Booking> findPendingBookingsExpiredBefore(LocalDateTime expiryTime) {
        return bookingJpaRepository.findByStatusAndExpiresAtBefore("PENDING", expiryTime)
                .stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByCustomerId(CustomerId customerId) {
        return bookingJpaRepository.findByCustomerId(customerId.value())
                .stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }
}
