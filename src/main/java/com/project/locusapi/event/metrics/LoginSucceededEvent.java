package com.project.locusapi.event.metrics;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoginSucceededEvent(
        UUID userId,
        String email,
        String userAgent,
        LocalDateTime occurredAt
) {
}
