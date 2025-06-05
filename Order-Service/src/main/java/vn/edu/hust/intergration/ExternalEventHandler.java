package vn.edu.hust.intergration;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.edu.hust.application.dto.command.MarkOrderPaidCommand;
import vn.edu.hust.domain.event.PaymentCompletedEvent;

@Component
public class ExternalEventHandler {

    @Autowired
    private CommandGateway commandGateway;

    @KafkaListener(topics = "payment-events.completed", groupId = "order-service")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        MarkOrderPaidCommand command = new MarkOrderPaidCommand();
        command.setOrderId(event.orderId());
        command.setPaymentId(event.paymentId());

        commandGateway.sendAndWait(command);
    }
}
