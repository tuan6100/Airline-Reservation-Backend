package vn.edu.hust.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.SimpleQueryBus;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.edu.hust.domain.model.aggregate.Order;

@Configuration
public class AxonConfig {

    @Bean
    public EventStore eventStore() {
        return EmbeddedEventStore.builder()
                .storageEngine(new InMemoryEventStorageEngine())
                .build();
    }

    @Bean
    public Serializer serializer() {
        return JacksonSerializer.builder()
                .objectMapper(new ObjectMapper().findAndRegisterModules())
                .build();
    }

    @Bean
    public EventSourcingRepository<Order> orderRepository(EventStore eventStore) {
        return EventSourcingRepository.builder(Order.class)
                .eventStore(eventStore)
                .build();
    }

    @Bean
    public CommandBus commandBus() {
        return SimpleCommandBus.builder().build();
    }

    @Bean
    public QueryBus queryBus() {
        return SimpleQueryBus.builder().build();
    }

    @Bean
    public EventBus eventBus() {
        return SimpleEventBus.builder().build();
    }
}
