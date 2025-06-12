package vn.edu.hust.infrastructure.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hust.application.dto.query.OrderDTO;
import vn.edu.hust.application.dto.query.OrderSummaryDTO;
import vn.edu.hust.application.dto.query.OrderItemDTO;
import vn.edu.hust.infrastructure.entity.OrderItemEntity;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OrderQueryRepository {

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    public OrderDTO findById(Long orderId) {
        OrderEntity orderEntity = orderJpaRepository.findById(orderId).orElse(null);
        if (orderEntity == null) {
            return null;
        }

        OrderDTO dto = new OrderDTO();
        dto.setOrderId(orderEntity.getOrderId());
        dto.setCustomerId(orderEntity.getCustomerId());
        dto.setBookingId((orderEntity.getBookingId()));
        dto.setPromotionId(orderEntity.getPromotionId());
        dto.setStatus(orderEntity.getStatus());
        dto.setPaymentStatus(orderEntity.getPaymentStatus());
        dto.setTotalAmount(orderEntity.getTotalAmount());
        dto.setCurrency(orderEntity.getCurrency());
        dto.setCreatedAt(orderEntity.getCreatedAt());
        dto.setUpdatedAt(orderEntity.getUpdatedAt());

        // Load order items
        List<OrderItemEntity> itemEntities = orderItemJpaRepository.findByOrderId(orderId);
        List<OrderItemDTO> itemDTOs = itemEntities.stream()
                .map(this::convertToOrderItemDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    public List<OrderSummaryDTO> findByCustomerId(Long customerId) {
        return orderJpaRepository.findByCustomerId(customerId)
                .stream()
                .map(this::convertToOrderSummaryDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO findByBookingId(String bookingId) {
        OrderEntity orderEntity = orderJpaRepository.findByBookingId(bookingId).orElse(null);
        if (orderEntity == null) {
            return null;
        }
        return findById(orderEntity.getOrderId());
    }

    private OrderItemDTO convertToOrderItemDTO(OrderItemEntity entity) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(entity.getId());
        dto.setTicketId(entity.getTicketId());
        dto.setFlightId(entity.getFlightId());
        dto.setSeatId(entity.getSeatId());
        dto.setPrice(entity.getPrice());
        dto.setCurrency(entity.getCurrency());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    private OrderSummaryDTO convertToOrderSummaryDTO(OrderEntity entity) {
        OrderSummaryDTO dto = new OrderSummaryDTO();
        dto.setOrderId(entity.getOrderId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setBookingId(entity.getBookingId());
        dto.setStatus(entity.getStatus());
        dto.setPaymentStatus(entity.getPaymentStatus());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setCreatedAt(entity.getCreatedAt());

        // Count items
        List<OrderItemEntity> items = orderItemJpaRepository.findByOrderId(entity.getOrderId());
        dto.setItemCount(items.size());

        return dto;
    }
}