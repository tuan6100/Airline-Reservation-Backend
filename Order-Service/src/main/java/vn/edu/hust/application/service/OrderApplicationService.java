package vn.edu.hust.application.service;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.hust.application.dto.command.*;
import vn.edu.hust.application.dto.query.OrderDTO;
import vn.edu.hust.application.dto.query.OrderSummaryDTO;
import vn.edu.hust.application.dto.query.GetOrderByBookingQuery;
import vn.edu.hust.application.dto.query.GetOrderQuery;
import vn.edu.hust.application.dto.query.GetOrdersByCustomerQuery;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderApplicationService {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private QueryGateway queryGateway;

    public CompletableFuture<Long> createOrder(CreateOrderCommand command) {
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> addOrderItem(AddOrderItemCommand command) {
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> confirmOrder(Long orderId) {
        ConfirmOrderCommand command = new ConfirmOrderCommand();
        command.setOrderId(orderId);
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> cancelOrder(Long orderId, String reason) {
        CancelOrderCommand command = new CancelOrderCommand();
        command.setOrderId(orderId);
        command.setReason(reason);
        return commandGateway.send(command);
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