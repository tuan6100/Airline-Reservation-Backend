package vn.edu.hust.domain.repository;

import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.aggregate.Booking;
import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.domain.model.valueobj.CustomerId;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository {
    Booking findById(BookingId bookingId);
    void save(Booking booking);
    List<Booking> findPendingBookingsExpiredBefore(LocalDateTime expiryTime);
    List<Booking> findByCustomerId(CustomerId customerId);
}

