package com.project.locusapi.repository.metrics.projection;

import java.time.LocalDate;

public interface TimeBucketMetricProjection {
    LocalDate getBucket();
    Long getTotal();
}
