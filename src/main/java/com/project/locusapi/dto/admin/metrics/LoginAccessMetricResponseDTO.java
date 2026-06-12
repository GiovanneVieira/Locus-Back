package com.project.locusapi.dto.admin.metrics;

import com.project.locusapi.constant.metrics.LoginDeviceType;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoginAccessMetricResponseDTO(
        UUID id,
        UUID userId,
        String email,
        boolean success,
        String reason,
        LoginDeviceType deviceType,
        String operatingSystem,
        LocalDateTime occurredAt
) {
}
