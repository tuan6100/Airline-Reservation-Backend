package vn.edu.hust.integration.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.edu.hust.application.service.BookingApplicationService;
import vn.edu.hust.integration.event.PaymentCompletedEvent;
import vn.edu.hust.integration.event.PaymentFailedEvent;

@Component
public class PaymentServiceEventListener {

    @Autowired private BookingApplicationService bookingService;


    public PaymentServiceEventListener(BookingApplicationService bookingService) {
        this.bookingService = bookingService;
    }

    @KafkaListener(topics = "payment-events.completed", groupId = "booking-service")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        // Payment completed, booking can be confirmed if not already
        // This might be redundant if order service handles this
        // Could implement as a fallback mechanism
    }

    @KafkaListener(topics = "payment-events.failed", groupId = "booking-service")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        // Payment failed, booking might need to be cancelled
        // This might be redundant if order service handles this
        // Could implement as a fallback mechanism
    }
}