package com.project.locusapi.event.metrics;

import java.time.LocalDateTime;

public record LoginFailedEvent(
        String email,
        String userAgent,
        String reason,
        LocalDateTime occurredAt
) {
}
