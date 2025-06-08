package vn.edu.hust.application.service;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.application.dto.command.*;
import vn.edu.hust.application.dto.query.OrderDTO;
import vn.edu.hust.application.dto.query.OrderSummaryDTO;
import vn.edu.hust.application.dto.query.GetOrderByBookingQuery;
import vn.edu.hust.application.dto.query.GetOrderQuery;
import vn.edu.hust.application.dto.query.GetOrdersByCustomerQuery;
import vn.edu.hust.domain.model.valueobj.OrderId;
import vn.edu.hust.infrastructure.dto.PaymentResponseDTO;
import vn.edu.hust.infrastructure.service.PaymentServiceClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class OrderApplicationService {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private QueryGateway queryGateway;

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    @Transactional
    public CompletableFuture<Long> createOrder(CreateOrderCommand command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return commandGateway.sendAndWait(command);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create order", e);
            }
        });
    }

    public CompletableFuture<Void> addOrderItem(AddOrderItemCommand command) {
        return commandGateway.send(command);
    }

    @Transactional
    public CompletableFuture<Void> confirmOrderAndInitiatePayment(Long orderId) {
        return CompletableFuture.runAsync(() -> {
            try {
                ConfirmOrderCommand confirmCommand = new ConfirmOrderCommand();
                confirmCommand.setOrderId(orderId);
                commandGateway.sendAndWait(confirmCommand);
                OrderDTO order = getOrder(orderId).join();
                if (order == null) {
                    throw new IllegalArgumentException("Order not found: " + orderId);
                }
                PaymentResponseDTO paymentResponse = paymentServiceClient.initiatePayment(
                        new OrderId(orderId),
                        order.getTotalAmount(),
                        order.getCurrency(),
                        order.getCustomerId()
                );
                MarkOrderPaymentPendingCommand pendingCommand = new MarkOrderPaymentPendingCommand();
                pendingCommand.setOrderId(orderId);
                commandGateway.sendAndWait(pendingCommand);
                OrderApplicationService.log.info("Payment initiated for order {} with payment ID: {}", orderId, paymentResponse.getPaymentId());
            } catch (Exception e) {
                throw new RuntimeException("Failed to confirm order and initiate payment", e);
            }
        });
    }

    public CompletableFuture<Void> confirmOrder(Long orderId) {
        ConfirmOrderCommand command = new ConfirmOrderCommand();
        command.setOrderId(orderId);
        return commandGateway.send(command);
    }

    public void cancelOrder(Long orderId, String reason) {
        CancelOrderCommand command = new CancelOrderCommand();
        command.setOrderId(orderId);
        command.setReason(reason);
        commandGateway.send(command);
    }

    public CompletableFuture<Void> markOrderPaid(Long orderId, Long paymentId) {
        MarkOrderPaidCommand command = new MarkOrderPaidCommand();
        command.setOrderId(orderId);
        command.setPaymentId(paymentId);
        return commandGateway.send(command);
    }

    public CompletableFuture<OrderDTO> getOrder(Long orderId) {
        GetOrderQuery query = new GetOrderQuery();
        query.setOrderId(orderId);
        return queryGateway.query(query, OrderDTO.class);
    }

    public CompletableFuture<List<OrderSummaryDTO>> getOrdersByCustomer(Long customerId) {
        GetOrdersByCustomerQuery query = new GetOrdersByCustomerQuery();
        query.setCustomerId(customerId);
        return queryGateway.query(query, ResponseTypes.multipleInstancesOf(OrderSummaryDTO.class));
    }

    public CompletableFuture<OrderDTO> getOrderByBooking(String bookingId) {
        GetOrderByBookingQuery query = new GetOrderByBookingQuery();
        query.setBookingId(bookingId);
        return queryGateway.query(query, OrderDTO.class);
    }
}
