package com.project.locusapi.dto.admin.metrics;

public record RentalConversionMetricsDTO(
        long createdRentals,
        long confirmedRentals,
        double conversionRate
) {
}
