package vn.edu.hust.intergration.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.edu.hust.application.service.OrderApplicationService;
import vn.edu.hust.domain.event.PaymentCompletedEvent;

@Component
public class PaymentServiceEventListener {

    @Autowired
    private OrderApplicationService orderService;

    @KafkaListener(topics = "payment-events.completed", groupId = "booking-service")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        orderService.confirmOrder(event.orderId());
    }

    @KafkaListener(topics = "payment-events.failed", groupId = "booking-service")
    public void handlePaymentFailed(PaymentFailedEvent event) {

    }
}