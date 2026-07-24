package com.apiflow.eventcollector.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record EventRequest(
        @NotBlank(message = "serviceName is required")
        String serviceName,

        @NotBlank(message = "method is required")
        String method,

        @NotBlank(message = "path is required")
        String path,

        int statusCode,

        @Min(value = 0, message = "latencyMs must be >= 0")
        long latencyMs
) {
}
