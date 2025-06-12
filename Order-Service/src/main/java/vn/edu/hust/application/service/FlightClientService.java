package vn.edu.hust.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.edu.hust.application.dto.query.FlightDetailsDTO;
import vn.edu.hust.domain.exception.ServiceIntegrationException;

@Service
public class FlightClientService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.flight.url}/api/flights/")
    private String flightServiceUrl;

    public String getDepartureNation(Long flightId) {
        String endpoint = flightServiceUrl + "v1/{flightId}/departure-nation";
        try {
            return restTemplate.getForObject(
                    endpoint,
                    String.class,
                    flightId
            );
        } catch (RestClientException e) {
            throw new ServiceIntegrationException("Failed to get departure country of the flight. " + e.getMessage());
        }
    }
}
