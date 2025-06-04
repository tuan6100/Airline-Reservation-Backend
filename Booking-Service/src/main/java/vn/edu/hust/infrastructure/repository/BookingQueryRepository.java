package vn.edu.hust.infrastructure.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hust.application.dto.query.BookingDTO;
import vn.edu.hust.infrastructure.entity.BookingEntity;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BookingQueryRepository {

    @Autowired
    private BookingJpaRepository bookingRepository;

    public BookingDTO findByBookingId(String bookingId) {
        BookingEntity bookingEntity = bookingRepository.findById(bookingId).get();
        BookingDTO dto = new BookingDTO();
        dto.setBookingId(bookingEntity.getBookingId());
        dto.setCustomerId(bookingEntity.getCustomerId());
        dto.setStatus(bookingEntity.getStatus());
        dto.setCreatedAt(bookingEntity.getCreatedAt());
        dto.setExpiresAt(bookingEntity.getExpiresAt());
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
                    return dto;
                })
                .collect(Collectors.toList());
    }
}