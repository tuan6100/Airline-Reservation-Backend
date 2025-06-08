package vn.edu.hust.intergration.event;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.application.dto.command.CancelOrderCommand;
import vn.edu.hust.application.dto.command.MarkOrderPaidCommand;

@Slf4j
@Component
public class PaymentEventHandler {

    @Autowired
    private CommandGateway commandGateway;

    @KafkaListener(topics = "payment-events.completed", groupId = "order-service")
    @Transactional
    public void handlePaymentCompleted(String message) {
        try {
            log.info("Received payment completed event: {}", message);
            Long orderId = extractOrderId(message);
            Long paymentId = extractPaymentId(message);
            if (orderId != null && paymentId != null) {
                MarkOrderPaidCommand command = new MarkOrderPaidCommand();
                command.setOrderId(orderId);
                command.setPaymentId(paymentId);
                commandGateway.sendAndWait(command);
                log.info("Order {} marked as paid with payment {}", orderId, paymentId);
            }

        } catch (Exception e) {
            log.error("Failed to handle payment completed event: {}", message, e);
        }
    }

    @KafkaListener(topics = "payment-events.failed", groupId = "order-service")
    @Transactional
    public void handlePaymentFailed(String message) {
        try {
            log.info("Received payment failed event: {}", message);
            Long orderId = extractOrderId(message);
            String reason = extractFailureReason(message);
            if (orderId != null) {
                CancelOrderCommand command = new CancelOrderCommand();
                command.setOrderId(orderId);
                command.setReason("PAYMENT_FAILED: " + (reason != null ? reason : "Unknown reason"));

                commandGateway.sendAndWait(command);
                log.info("Order {} cancelled due to payment failure", orderId);
            }

        } catch (Exception e) {
            log.error("Failed to handle payment failed event: {}", message, e);
        }
    }

    private Long extractOrderId(String message) {
        try {
            String pattern = "\"orderId\"\\s*:\\s*(\\d+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(message);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
        } catch (Exception e) {
            log.error("Failed to extract orderId from message: {}", message);
        }
        return null;
    }

    private Long extractPaymentId(String message) {
        try {
            String pattern = "\"paymentId\"\\s*:\\s*(\\d+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(message);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
        } catch (Exception e) {
            log.error("Failed to extract paymentId from message: {}", message);
        }
        return null;
    }

    private String extractFailureReason(String message) {
        try {
            String pattern = "\"failureReason\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(message);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            log.error("Failed to extract failure reason from message: {}", message);
        }
        return null;
    }
}