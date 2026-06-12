package com.project.locusapi.dto.admin.metrics;

import com.project.locusapi.constant.metrics.CriticalFailureType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CriticalFailureMetricResponseDTO(
        UUID id,
        String email,
        CriticalFailureType failureType,
        String reason,
        LocalDateTime occurredAt
) {
}
