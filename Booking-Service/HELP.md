booking-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── airline/
│   │   │           └── booking/
│   │   │               ├── BookingServiceApplication.java
│   │   │               ├── domain/
│   │   │               │   ├── model/
│   │   │               │   │   ├── aggregate/
│   │   │               │   │   │   ├── Booking.java
│   │   │               │   │   │   └── Seat.java
│   │   │               │   │   ├── entity/
│   │   │               │   │   ├── valueobject/
│   │   │               │   │   │   ├── BookingId.java
│   │   │               │   │   │   ├── SeatId.java
│   │   │               │   │   │   ├── FlightId.java
│   │   │               │   │   │   ├── CustomerId.java
│   │   │               │   │   │   ├── AircraftId.java
│   │   │               │   │   │   ├── SeatClassId.java
│   │   │               │   │   │   ├── SeatCode.java
│   │   │               │   │   │   ├── SeatReservation.java
│   │   │               │   │   │   ├── Money.java
│   │   │               │   │   │   └── SeatClass.java
│   │   │               │   │   └── enumeration/
│   │   │               │   │       ├── BookingStatus.java
│   │   │               │   │       ├── SeatStatus.java
│   │   │               │   │       └── CancellationReason.java
│   │   │               │   ├── event/
│   │   │               │   │   ├── BookingCreatedEvent.java
│   │   │               │   │   ├── BookingConfirmedEvent.java
│   │   │               │   │   ├── BookingCancelledEvent.java
│   │   │               │   │   ├── BookingExpiredEvent.java
│   │   │               │   │   ├── SeatHeldEvent.java
│   │   │               │   │   ├── SeatReservedEvent.java
│   │   │               │   │   ├── SeatReleasedEvent.java
│   │   │               │   │   ├── SeatAddedToBookingEvent.java
│   │   │               │   │   └── SeatRemovedFromBookingEvent.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── BookingRepository.java
│   │   │               │   │   └── SeatRepository.java
│   │   │               │   ├── service/
│   │   │               │   │   └── BookingDomainService.java
│   │   │               │   └── exception/
│   │   │               │       ├── SeatNotAvailableException.java
│   │   │               │       ├── BookingExpiredException.java
│   │   │               │       └── SeatLockingException.java
│   │   │               ├── application/
│   │   │               │   ├── service/
│   │   │               │   │   └── BookingApplicationService.java
│   │   │               │   ├── dto/
│   │   │               │   │   ├── command/
│   │   │               │   │   │   ├── CreateBookingCommand.java
│   │   │               │   │   │   └── SeatSelectionDTO.java
│   │   │               │   │   └── query/
│   │   │               │   │       ├── BookingDTO.java
│   │   │               │   │       ├── SeatReservationDTO.java
│   │   │               │   │       └── SeatDTO.java
│   │   │               │   └── exception/
│   │   │               │       └── EntityNotFoundException.java
│   │   │               ├── infrastructure/
│   │   │               │   ├── repository/
│   │   │               │   │   ├── JpaBookingRepository.java
│   │   │               │   │   ├── JpaSeatRepository.java
│   │   │               │   │   ├── BookingJpaRepository.java
│   │   │               │   │   └── SeatJpaRepository.java
│   │   │               │   ├── entity/
│   │   │               │   │   ├── BookingEntity.java
│   │   │               │   │   ├── SeatReservationEntity.java
│   │   │               │   │   └── SeatEntity.java
│   │   │               │   ├── mapper/
│   │   │               │   │   ├── BookingMapper.java
│   │   │               │   │   ├── SeatReservationMapper.java
│   │   │               │   │   └── SeatMapper.java
│   │   │               │   ├── event/
│   │   │               │   │   ├── DomainEventPublisher.java
│   │   │               │   │   ├── KafkaConfig.java
│   │   │               │   │   └── KafkaEventPublisher.java
│   │   │               │   ├── service/
│   │   │               │   │   ├── FlightServiceClient.java
│   │   │               │   │   ├── RedisLockService.java
│   │   │               │   │   └── SeatLockingService.java
│   │   │               │   └── exception/
│   │   │               │       └── ServiceIntegrationException.java
│   │   │               ├── presentation/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── BookingController.java
│   │   │               │   │   └── SeatController.java
│   │   │               │   ├── advice/
│   │   │               │   │   └── GlobalExceptionHandler.java
│   │   │               │   └── dto/
│   │   │               │       └── ErrorResponse.java
│   │   │               └── integration/
│   │   │                   ├── event/
│   │   │                   │   ├── OrderConfirmedEvent.java
│   │   │                   │   ├── OrderCancelledEvent.java
│   │   │                   │   ├── PaymentCompletedEvent.java
│   │   │                   │   └── PaymentFailedEvent.java
│   │   │                   └── listener/
│   │   │                       ├── OrderServiceEventListener.java
│   │   │                       └── PaymentServiceEventListener.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__init_schema.sql
│   │               └── V2__add_booking_tables.sql
│   └── test/
│       └── java/
│           └── com/
│               └── airline/
│                   └── booking/
│                       ├── domain/
│                       │   ├── model/
│                       │   │   ├── BookingTest.java
│                       │   │   └── SeatTest.java
│                       │   └── service/
│                       │       └── BookingDomainServiceTest.java
│                       ├── application/
│                       │   └── service/
│                       │       └── BookingApplicationServiceTest.java
│                       └── presentation/
│                           └── controller/
│                               └── BookingControllerIntegrationTest.java
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md

