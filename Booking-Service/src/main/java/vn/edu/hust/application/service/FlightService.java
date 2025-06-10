package vn.edu.hust.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.edu.hust.application.dto.query.FlightDetailsDTO;
import vn.edu.hust.domain.model.valueobj.FlightId;
import vn.edu.hust.infrastructure.exception.ServiceIntegrationException;

@Service
public class FlightService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.flight.url}")
    private String flightServiceUrl;

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


}
