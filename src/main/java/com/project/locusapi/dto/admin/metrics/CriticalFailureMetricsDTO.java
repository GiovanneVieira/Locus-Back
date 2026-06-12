package com.project.locusapi.dto.admin.metrics;

import java.util.List;

public record CriticalFailureMetricsDTO(
        long totalFailures,
        List<MetricCountDTO> failuresByType
) {
}
