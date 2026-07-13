package com.apiflow.eventcollector.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/v1/events/ping")
    public Map<String, String> ping() {
        return Map.of("status", "event-collector-service is alive");
    }
}