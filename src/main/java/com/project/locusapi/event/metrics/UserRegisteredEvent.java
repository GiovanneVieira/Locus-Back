package com.project.locusapi.event.metrics;

import com.project.locusapi.constant.AuthProvider;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID userId,
        String email,
        boolean enabled,
        AuthProvider authProvider,
        LocalDateTime occurredAt
) {
}
