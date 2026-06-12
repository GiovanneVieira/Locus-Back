package com.project.locusapi.dto.admin.metrics;

import java.time.LocalDateTime;

public record AdminMetricsOverviewDTO(
        LocalDateTime start,
        LocalDateTime end,
        UserAcquisitionMetricsDTO users,
        AccessPlatformMetricsDTO accessPlatforms,
        RentalConversionMetricsDTO rentals,
        StorageUploadMetricsDTO storageUploads,
        CriticalFailureMetricsDTO criticalFailures
) {
}
