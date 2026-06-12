package com.project.locusapi.event.metrics;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserActivatedEvent(
        UUID userId,
        String email,
        LocalDateTime occurredAt
) {
}
