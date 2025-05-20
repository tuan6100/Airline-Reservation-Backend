package vn.edu.hust.infrastructure.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DomainEventPublisher {
    private static DomainEventPublisher instance;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        instance = this;
    }

    public static DomainEventPublisher instance() {
        return instance;
    }

    public void publish(Object event) {
        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(event);
        }
    }
}

