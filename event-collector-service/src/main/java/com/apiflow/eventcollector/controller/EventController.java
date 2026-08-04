package com.apiflow.eventcollector.controller;

import com.apiflow.eventcollector.dto.ApiCallEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.apiflow.eventcollector.config.KafkaTopicConfig.API_CALL_EVENTS_TOPIC;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, String>> ingest(@Valid @RequestBody ApiCallEvent event) {
        kafkaTemplate.send(API_CALL_EVENTS_TOPIC, event.serviceName(), event);
        log.info("Published event: {} {} -> {}", event.httpMethod(), event.endpoint(), event.statusCode());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("status", "event accepted"));
    }
}
