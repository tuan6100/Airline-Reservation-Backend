package vn.edu.hust.infrastructure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.hust.infrastructure.dto.BookingDTO;

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

    public BookingDTO getBookingDetails(String bookingId) {  // Changed parameter type
        String url = bookingServiceUrl + "/api/bookings/" + bookingId;
        ResponseEntity<BookingDTO> response = restTemplate.getForEntity(url, BookingDTO.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to get booking details: " + response.getStatusCode());
        }
    }

    public void confirmBooking(String bookingId) {  // Changed parameter type
        String url = bookingServiceUrl + "/api/bookings/" + bookingId + "/confirm";
        ResponseEntity<Void> response = restTemplate.postForEntity(url, null, Void.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to confirm booking: " + response.getStatusCode());
        }
    }

    public void cancelBooking(String bookingId, String reason) {  // Changed parameter type
        String url = bookingServiceUrl + "/api/bookings/" + bookingId + "/cancel?reason=" + reason;
        ResponseEntity<Void> response = restTemplate.postForEntity(url, null, Void.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to cancel booking: " + response.getStatusCode());
        }
    }
}