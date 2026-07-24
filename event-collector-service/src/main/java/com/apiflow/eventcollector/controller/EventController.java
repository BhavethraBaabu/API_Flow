package com.apiflow.eventcollector.controller;

import com.apiflow.eventcollector.dto.EventRequest;
import com.apiflow.eventcollector.model.ApiEvent;
import com.apiflow.eventcollector.service.EventProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventProducerService eventProducerService;

    @PostMapping
    public ResponseEntity<ApiEvent> ingest(@Valid @RequestBody EventRequest request) {
        ApiEvent event = ApiEvent.of(
                request.serviceName(),
                request.method(),
                request.path(),
                request.statusCode(),
                request.latencyMs()
        );

        eventProducerService.publish(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(event);
    }
}
