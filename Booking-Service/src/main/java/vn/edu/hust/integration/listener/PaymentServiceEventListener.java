package vn.edu.hust.integration.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.edu.hust.application.service.BookingApplicationService;
import vn.edu.hust.integration.event.PaymentCompletedEvent;
import vn.edu.hust.integration.event.PaymentFailedEvent;

@Component
public class PaymentServiceEventListener {

    @Autowired
    private BookingApplicationService bookingService;

    @KafkaListener(topics = "order-events.confirmed", groupId = "booking-service")
    public void handleOrderConfirmed(PaymentCompletedEvent event) {
        bookingService.confirmBooking(event.bookingId());
    }

    @KafkaListener(topics = "payment-events.failed", groupId = "booking-service")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        bookingService.cancelBooking(
                event.bookingId(),
                event.errorMessage() != null ? event.errorMessage() : "PAYMENT_FAILED"
        );
    }
}