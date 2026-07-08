package com.apiflow.authentication.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserSummary user
) {
    @Builder
    public record UserSummary(
            Long id,
            String fullName,
            String email,
            String role
    ) {
    }
}