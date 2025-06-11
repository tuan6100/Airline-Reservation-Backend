package vn.edu.hust.integration.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.edu.hust.application.service.BookingApplicationService;
import vn.edu.hust.integration.event.OrderCancelledEvent;
import vn.edu.hust.integration.event.OrderConfirmedEvent;

@Component
public class OrderServiceEventListener {

    @Autowired
    private BookingApplicationService bookingService;

    @KafkaListener(topics = "order-events.confirmed", groupId = "booking-service")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        bookingService.confirmBooking(event.bookingId());
    }

    @KafkaListener(topics = "order-events.cancelled", groupId = "booking-service")
    public void handleOrderCancelled(OrderCancelledEvent event) {
        bookingService.cancelBooking(
                event.bookingId(),
                event.reason() != null ? event.reason() : "ORDER_CANCELLED"
        );
    }
}
