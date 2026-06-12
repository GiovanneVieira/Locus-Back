package com.project.locusapi.dto.admin.metrics;

import java.util.List;

public record UserAcquisitionMetricsDTO(
        long newUsersDaily,
        long newUsersWeekly,
        long newUsersMonthly,
        long activatedUsersInRange,
        List<TimeBucketMetricDTO> registrationSeries
) {
}
