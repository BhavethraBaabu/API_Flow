package com.apiflow.eventcollector.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String API_CALL_EVENTS_TOPIC = "api-call-events";

    @Bean
    public NewTopic apiCallEventsTopic() {
        return TopicBuilder.name(API_CALL_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
