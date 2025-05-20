package vn.edu.hust.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.edu.hust.application.dto.query.FlightDetailsDTO;
import vn.edu.hust.domain.model.valueobj.FlightId;
import vn.edu.hust.domain.model.valueobj.Money;
import vn.edu.hust.domain.model.valueobj.SeatClassId;
import vn.edu.hust.infrastructure.exception.ServiceIntegrationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

@Service
public class FlightService {

    private final RestTemplate restTemplate;
    private final String flightServiceUrl;
    private final Currency defaultCurrency;

    @Autowired
    public FlightService(
            RestTemplate restTemplate,
            @Value("${services.flight.url}") String flightServiceUrl,
            @Value("${services.flight.default-currency:VND}") String defaultCurrencyCode) {
        this.restTemplate = restTemplate;
        this.flightServiceUrl = flightServiceUrl;
        this.defaultCurrency = Currency.getInstance(defaultCurrencyCode);
    }

    public FlightDetailsDTO getFlightDetails(FlightId flightId) {
        try {
            return restTemplate.getForObject(
                    flightServiceUrl + "/api/flights/{flightId}",
                    FlightDetailsDTO.class,
                    flightId.value()
            );
        } catch (RestClientException e) {
            throw new ServiceIntegrationException("Failed to get flight details");
        }
    }

    public boolean isFlightAvailable(FlightId flightId, LocalDateTime departureDate) {
        try {
            return Boolean.TRUE.equals(restTemplate.getForObject(
                    flightServiceUrl + "/api/flights/{flightId}/available?date={date}",
                    Boolean.class,
                    flightId.value(),
                    departureDate.format(DateTimeFormatter.ISO_DATE_TIME)
            ));
        } catch (RestClientException e) {
            throw new ServiceIntegrationException("Failed to check flight availability");
        }
    }

    public List<FlightDetailsDTO> searchFlights(
            String departureAirportCode,
            String arrivalAirportCode,
            LocalDateTime departureDate,
            int passengerCount) {
        try {
            FlightDetailsDTO[] flights = restTemplate.getForObject(
                    flightServiceUrl + "/api/flights/search" +
                            "?departure={departure}" +
                            "&arrival={arrival}" +
                            "&date={date}" +
                            "&passengers={passengers}",
                    FlightDetailsDTO[].class,
                    departureAirportCode,
                    arrivalAirportCode,
                    departureDate.format(DateTimeFormatter.ISO_DATE_TIME),
                    passengerCount
            );

            return flights != null ? Arrays.asList(flights) : Collections.emptyList();
        } catch (RestClientException e) {
            throw new ServiceIntegrationException("Failed to search flights");
        }
    }

    public Money getFlightBasePrice(FlightId flightId, SeatClassId seatClassId) {
        try {
            FlightDetailsDTO flightDetails = getFlightDetails(flightId);

            if (flightDetails == null) {
                throw new ServiceIntegrationException("Flight not found: " + flightId.value());
            }

            // Determine price based on seat class
            double basePrice = switch (seatClassId.value().intValue()) {
                case 1 -> // Economy
                        flightDetails.getBaseEconomyPrice();
                case 2 -> // Business
                        flightDetails.getBaseBusinessPrice();
                case 3 -> // First Class
                        flightDetails.getBaseFirstClassPrice();
                default -> flightDetails.getBaseEconomyPrice();
            };

            return new Money(new BigDecimal(basePrice), defaultCurrency);
        } catch (RestClientException e) {
            throw new ServiceIntegrationException("Failed to get flight base price");
        }
    }
}
