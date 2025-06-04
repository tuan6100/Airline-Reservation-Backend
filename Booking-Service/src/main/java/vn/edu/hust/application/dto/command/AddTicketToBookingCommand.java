package vn.edu.hust.application.dto.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
public class AddTicketToBookingCommand {
    @TargetAggregateIdentifier
    private String bookingId;
    private Long ticketId;
    private Long seatId;
    private Double price;
    private String currency;
}