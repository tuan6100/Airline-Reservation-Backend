package vn.edu.hust.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AxonConfig {

//    @Bean
//    public EventStore eventStore() {
//        return EmbeddedEventStore.builder()
//                .storageEngine(new InMemoryEventStorageEngine())
//                .build();
//    }

    @Bean
    @Primary
    public Serializer serializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();

        return JacksonSerializer.builder()
                .objectMapper(objectMapper)
                .build();
    }

}