package vn.edu.hust.application.dto.query;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetTicketsByFlightQuery {
    private Long flightId;
}
