package com.project.locusapi.dto.admin.metrics;

import java.util.List;

public record AccessPlatformMetricsDTO(
        long successfulLogins,
        long failedLogins,
        List<MetricCountDTO> deviceDistribution,
        List<MetricCountDTO> operatingSystemDistribution
) {
}
