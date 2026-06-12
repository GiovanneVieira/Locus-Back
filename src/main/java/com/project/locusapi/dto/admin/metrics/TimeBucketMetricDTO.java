package com.project.locusapi.dto.admin.metrics;

import java.time.LocalDate;

public record TimeBucketMetricDTO(
        LocalDate bucket,
        long total
) {
}
