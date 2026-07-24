package com.apiflow.eventcollector.model;

import java.time.Instant;
import java.util.UUID;

public record ApiEvent(
        String eventId,
        String serviceName,
        String method,
        String path,
        int statusCode,
        long latencyMs,
        Instant occurredAt
) {
    public static ApiEvent of(String serviceName, String method, String path, int statusCode, long latencyMs) {
        return new ApiEvent(
                UUID.randomUUID().toString(),
                serviceName,
                method,
                path,
                statusCode,
                latencyMs,
                Instant.now()
        );
    }
}
