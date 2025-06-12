package vn.edu.hust.application.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TicketBookedDTO extends TicketDTO {
    private String seatCode;
    private Long price;
}
