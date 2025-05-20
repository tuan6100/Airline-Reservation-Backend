package vn.edu.hust.infrastructure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.hust.domain.model.valueobj.BookingId;
import vn.edu.hust.infrastructure.dto.BookingDTO;

/**
 * Client for communication with Booking Service
 */
@Service
public class BookingServiceClient {

    private final RestTemplate restTemplate;
    private final String bookingServiceUrl;

    @Autowired
    public BookingServiceClient(
            RestTemplate restTemplate,
            @Value("${services.booking.url}") String bookingServiceUrl) {
        this.restTemplate = restTemplate;
        this.bookingServiceUrl = bookingServiceUrl;
    }

    /**
     * Get booking details
     */
    public BookingDTO getBookingDetails(BookingId bookingId) {
        String url = bookingServiceUrl + "/api/bookings/" + bookingId.value();
        ResponseEntity<BookingDTO> response = restTemplate.getForEntity(url, BookingDTO.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to get booking details: " + response.getStatusCode());
        }
    }

    /**
     * Confirm booking
     */
    public void confirmBooking(BookingId bookingId) {
        String url = bookingServiceUrl + "/api/bookings/" + bookingId.value() + "/confirm";
        ResponseEntity<Void> response = restTemplate.postForEntity(url, null, Void.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to confirm booking: " + response.getStatusCode());
        }
    }

    /**
     * Cancel booking
     */
    public void cancelBooking(BookingId bookingId, String reason) {
        String url = bookingServiceUrl + "/api/bookings/" + bookingId.value() + "/cancel?reason=" + reason;
        ResponseEntity<Void> response = restTemplate.postForEntity(url, null, Void.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to cancel booking: " + response.getStatusCode());
        }
    }
}