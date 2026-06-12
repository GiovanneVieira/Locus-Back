package com.project.locusapi.event.metrics;

import com.project.locusapi.constant.metrics.CriticalFailureType;

import java.time.LocalDateTime;

public record OtpValidationFailedEvent(
        String email,
        CriticalFailureType failureType,
        String reason,
        LocalDateTime occurredAt
) {
}
