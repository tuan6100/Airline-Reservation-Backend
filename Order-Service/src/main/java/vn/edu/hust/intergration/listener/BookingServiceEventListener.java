package vn.edu.hust.intergration.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import vn.edu.hust.application.dto.command.CreateOrderCommand;
import vn.edu.hust.intergration.restful.FlightClientService;
import vn.edu.hust.application.service.OrderApplicationService;
import vn.edu.hust.domain.event.BookingCancelledEvent;
import vn.edu.hust.intergration.event.OrderCreationRequestEvent;

@Component
public class BookingServiceEventListener {

    @Autowired
    private OrderApplicationService orderService;

    @Autowired
    private FlightClientService flightClientService;

    @RetryableTopic(
            attempts = "4",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            backoff = @Backoff(delay = 500, multiplier = 2),
            include = {Exception.class}
    )
    @KafkaListener(topics = "order-creation.topic", concurrency = "order-creation.ccr")
    public void handleOrderCreationRequest(@Payload OrderCreationRequestEvent event) {
        String nation = flightClientService.getDepartureNation(event.ticketBookedDTO().getFlightDetails().getFlightId());
        CreateOrderCommand command = new CreateOrderCommand();
        command.setBookingId(event.bookingId());
        command.setNation(nation);
        command.setItem(event.ticketBookedDTO());
    }

    @KafkaListener(topics = "booking-events.cancelled")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        orderService.cancelOrder(getOrderByBookingId(event.bookingId()), "BOOKING_CANCELLED");
    }

    private Long getOrderByBookingId(String bookingId) {
        return orderService.getOrderByBooking(bookingId)
                .thenApply(order -> order != null ? order.getOrderId() : null)
                .join();
    }


}