package vn.edu.hust.application.dto.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateTicketCommand {
    @TargetAggregateIdentifier
    private Long ticketId;
    private Long flightId;
    private LocalDateTime flightDepartureTime;
    private Long seatId;
    private UUID ticketCode;
}
