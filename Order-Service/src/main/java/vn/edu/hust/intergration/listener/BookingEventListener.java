package vn.edu.hust.intergration.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.edu.hust.application.service.OrderApplicationService;
import vn.edu.hust.application.dto.command.CreateOrderCommand;
import vn.edu.hust.domain.event.BookingCancelledEvent;
import vn.edu.hust.domain.event.BookingConfirmedEvent;

@Component
public class BookingEventListener {

    @Autowired
    private OrderApplicationService orderService;

    @KafkaListener(topics = "booking-events.confirmed", groupId = "order-service")
    public void handleBookingConfirmed(BookingConfirmedEvent event) {

    }

    @KafkaListener(topics = "booking-events.cancelled", groupId = "order-service")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        orderService.cancelOrder(getOrderByBookingId(event.bookingId()), "BOOKING_CANCELLED");
    }

    private Long getOrderByBookingId(String bookingId) {
        return orderService.getOrderByBooking(bookingId)
                .thenApply(order -> order != null ? order.getOrderId() : null)
                .join();
    }
}