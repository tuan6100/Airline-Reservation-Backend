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
import vn.edu.hust.domain.event.OrderConfirmedEvent;
import vn.edu.hust.domain.exception.PromotionExpiredException;
import vn.edu.hust.infrastructure.event.OrderEventPublisher;

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
    private OrderEventPublisher orderEventPublisher;

    public CompletableFuture<OrderDTO> getOrder(Long orderId) {
        GetOrderQuery query = new GetOrderQuery();
        query.setOrderId(orderId);
        return queryGateway.query(query, OrderDTO.class);
    }

    @Transactional
    public CompletableFuture<Long> applyOrderPromotion(Long orderId, Long promotionId) throws PromotionExpiredException {
        ApplyPromotionToOrderCommand command = new ApplyPromotionToOrderCommand();
        command.setOrderId(orderId);

        return commandGateway.send(command);
    }

    @Transactional
    public CompletableFuture<Void> confirmOrderAndInitiatePayment(Long orderId) {
        return CompletableFuture.runAsync(() -> {
            try {
                ConfirmOrderCommand confirmCommand = new ConfirmOrderCommand();
                confirmCommand.setOrderId(orderId);
                OrderConfirmedEvent event = commandGateway.sendAndWait(confirmCommand);
                orderEventPublisher.handleOrderConfirmedEvent(event);
            } catch (Exception e) {
                throw new RuntimeException("Failed to confirm order and initiate payment", e);
            }
        });
    }

    @Transactional
    public void removeItemFromOrder(Long orderId, List<Long> ticketIdList) {
        ticketIdList.stream().parallel().forEach(ticketId -> {
            RemoveItemFromOrderCommand removeItemFromOrderCommand = new RemoveItemFromOrderCommand(
                    orderId,
                    ticketId
            );
            commandGateway.send(removeItemFromOrderCommand);
        });
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
