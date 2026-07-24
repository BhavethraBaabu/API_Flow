package com.apiflow.eventcollector.service;

import com.apiflow.eventcollector.model.ApiEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventProducerService {

    private final KafkaTemplate<String, ApiEvent> kafkaTemplate;

    @Value("${apiflow.kafka.topic.api-events:api-events}")
    private String topic;

    public void publish(ApiEvent event) {
        kafkaTemplate.send(topic, event.serviceName(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event {} to {}", event.eventId(), topic, ex);
                    } else {
                        log.info("Published event {} to {} partition {} offset {}",
                                event.eventId(), topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
