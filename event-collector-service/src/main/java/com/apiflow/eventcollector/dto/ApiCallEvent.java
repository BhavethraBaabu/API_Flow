package com.apiflow.eventcollector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public record ApiCallEvent(
        @NotBlank(message = "serviceName is required")
        String serviceName,

        @NotBlank(message = "endpoint is required")
        String endpoint,

        @NotBlank(message = "httpMethod is required")
        String httpMethod,

        @NotNull(message = "statusCode is required")
        Integer statusCode,

        @PositiveOrZero(message = "latencyMs must be zero or positive")
        long latencyMs,

        Instant timestamp
) {
    public ApiCallEvent {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
