package com.apiflow.eventcollector;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.annotation.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"api-events"})
class EventControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        // spring.kafka.bootstrap-servers is auto-set by @EmbeddedKafka via the broker's system property
    }

    @Test
    void ingest_publishesEventToKafka() throws Exception {
        Map<String, Object> payload = Map.of(
                "serviceName", "authentication-service",
                "method", "POST",
                "path", "/api/v1/auth/login",
                "statusCode", 200,
                "latencyMs", 42
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isAccepted());

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        var consumerFactory = new org.springframework.kafka.core.DefaultKafkaConsumerFactory<String, String>(consumerProps);
        try (var consumer = consumerFactory.createConsumer()) {
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "api-events");
            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "api-events", Duration.ofSeconds(10));
            assertThat(record.value()).contains("authentication-service");
            assertThat(record.value()).contains("/api/v1/auth/login");
        }
    }
}
