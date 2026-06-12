package com.project.locusapi.event.metrics;

import java.time.LocalDateTime;
import java.util.UUID;

public record S3UploadTrackedEvent(
        UUID imageId,
        UUID hostId,
        long fileSize,
        String contentType,
        LocalDateTime occurredAt
) {
}
