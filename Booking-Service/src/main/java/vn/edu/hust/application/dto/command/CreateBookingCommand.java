package vn.edu.hust.application.dto.command;

import lombok.Data;

import java.util.List;

@Data
public class CreateBookingCommand {
    private Long customerId;
    private List<SeatSelectionDTO> seatSelections;
}