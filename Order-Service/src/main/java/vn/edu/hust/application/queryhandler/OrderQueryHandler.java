package vn.edu.hust.application.queryhandler;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.edu.hust.application.dto.query.*;
import vn.edu.hust.infrastructure.repository.OrderQueryRepository;

import java.util.List;

@Component
public class OrderQueryHandler {

    @Autowired
    private OrderQueryRepository orderQueryRepository;

    @QueryHandler
    public OrderDTO handle(GetOrderQuery query) {
        return orderQueryRepository.findById(query.getOrderId());
    }

    @QueryHandler
    public List<OrderSummaryDTO> handle(GetOrdersByCustomerQuery query) {
        return orderQueryRepository.findByCustomerId(query.getCustomerId());
    }

    @QueryHandler
    public OrderDTO handle(GetOrderByBookingQuery query) {
        return orderQueryRepository.findByBookingId(query.getBookingId());
    }
}

