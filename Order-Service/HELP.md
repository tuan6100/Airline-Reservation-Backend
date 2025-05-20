

order-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── vn/
│   │   │       └── edu/
│   │   │           └── hust/
│   │   │               ├── OrderServiceApplication.java
│   │   │               ├── application/
│   │   │               │   ├── OrderApplicationService.java
│   │   │               │   └── dto/
│   │   │               │       ├── command/
│   │   │               │       │   ├── CreateOrderCommand.java
│   │   │               │       │   └── UpdateOrderCommand.java
│   │   │               │       └── query/
│   │   │               │           ├── OrderDTO.java
│   │   │               │           ├── OrderItemDTO.java
│   │   │               │           └── OrderSummaryDTO.java
│   │   │               ├── domain/
│   │   │               │   ├── event/
│   │   │               │   │   ├── OrderCancelledEvent.java
│   │   │               │   │   ├── OrderConfirmedEvent.java
│   │   │               │   │   ├── OrderCreatedEvent.java
│   │   │               │   │   └── OrderStatusChangedEvent.java
│   │   │               │   ├── exception/
│   │   │               │   │   ├── OrderAlreadyConfirmedException.java
│   │   │               │   │   ├── OrderNotFoundException.java
│   │   │               │   │   └── PaymentFailedException.java
│   │   │               │   ├── model/
│   │   │               │   │   ├── aggregate/
│   │   │               │   │   │   └── Order.java
│   │   │               │   │   ├── entity/
│   │   │               │   │   │   └── OrderItem.java
│   │   │               │   │   ├── enumeration/
│   │   │               │   │   │   ├── OrderStatus.java
│   │   │               │   │   │   └── PaymentStatus.java
│   │   │               │   │   └── valueobj/
│   │   │               │   │       ├── BookingId.java
│   │   │               │   │       ├── CustomerId.java
│   │   │               │   │       ├── FlightId.java
│   │   │               │   │       ├── Money.java
│   │   │               │   │       ├── OrderId.java
│   │   │               │   │       ├── PromotionId.java
│   │   │               │   │       ├── SeatId.java
│   │   │               │   │       └── TicketId.java
│   │   │               │   ├── repository/
│   │   │               │   │   └── OrderRepository.java
│   │   │               │   └── service/
│   │   │               │       └── OrderDomainService.java
│   │   │               ├── infrastructure/
│   │   │               │   ├── configuration/
│   │   │               │   │   ├── KafkaConfig.java
│   │   │               │   │   └── RestTemplateConfig.java
│   │   │               │   ├── entity/
│   │   │               │   │   ├── OrderEntity.java
│   │   │               │   │   └── OrderItemEntity.java
│   │   │               │   ├── event/
│   │   │               │   │   ├── DomainEventPublisher.java
│   │   │               │   │   └── KafkaEventPublisher.java
│   │   │               │   ├── mapper/
│   │   │               │   │   ├── OrderItemMapper.java
│   │   │               │   │   └── OrderMapper.java
│   │   │               │   ├── outbox/
│   │   │               │   │   ├── OutboxMessage.java
│   │   │               │   │   └── OutboxService.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── JpaOrderRepository.java
│   │   │               │   │   └── OrderJpaRepository.java
│   │   │               │   └── service/
│   │   │               │       ├── BookingServiceClient.java
│   │   │               │       └── PaymentServiceClient.java
│   │   │               ├── integration/
│   │   │               │   ├── event/
│   │   │               │   │   ├── BookingConfirmedEvent.java
│   │   │               │   │   ├── BookingCreatedEvent.java
│   │   │               │   │   ├── PaymentCompletedEvent.java
│   │   │               │   │   └── PaymentFailedEvent.java
│   │   │               │   └── listener/
│   │   │               │       ├── BookingEventListener.java
│   │   │               │       └── PaymentEventListener.java
│   │   │               └── presentation/
│   │   │                   ├── controller/
│   │   │                   │   └── OrderController.java
│   │   │                   └── exception/
│   │   │                       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/
│   │           └── migration/
│   │               └── V1__init_schema.sql
│   └── test/
│       └── java/
│           └── vn/
│               └── edu/
│                   └── hust/
│                       ├── domain/
│                       │   ├── model/
│                       │   │   └── OrderTest.java
│                       │   └── service/
│                       │       └── OrderDomainServiceTest.java
│                       └── integration/
│                           └── OrderFlowIntegrationTest.java
└── pom.xml