package vn.edu.hust.infrastructure.service;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.edu.hust.application.dto.command.ExpireBookingCommand;
import vn.edu.hust.domain.model.enumeration.BookingStatus;
import vn.edu.hust.infrastructure.repository.BookingJpaRepository;

import java.time.LocalDateTime;

@Service
public class BookingExpirationService {

    @Autowired
    private BookingJpaRepository bookingRepository;

    @Autowired
    private CommandGateway commandGateway;

    @Scheduled(fixedDelay = 60000)
    public void expireBookings() {
        var expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(
                BookingStatus.PENDING,
                LocalDateTime.now()
        );

        expiredBookings.forEach(booking -> {
            ExpireBookingCommand command = new ExpireBookingCommand();
            command.setBookingId(booking.getBookingId());
            commandGateway.sendAndWait(command);
        });
    }
}