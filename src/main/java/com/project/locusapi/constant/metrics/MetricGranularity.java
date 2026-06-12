package com.project.locusapi.constant.metrics;

public enum MetricGranularity {
    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String dateTruncValue;

    MetricGranularity(String dateTruncValue) {
        this.dateTruncValue = dateTruncValue;
    }

    public String getDateTruncValue() {
        return dateTruncValue;
    }

    public static MetricGranularity fromParam(String value) {
        if (value == null || value.isBlank()) {
            return DAY;
        }

        for (MetricGranularity granularity : values()) {
            if (granularity.name().equalsIgnoreCase(value) || granularity.dateTruncValue.equalsIgnoreCase(value)) {
                return granularity;
            }
        }

        throw new IllegalArgumentException("Granularidade inválida. Use day, week ou month.");
    }
}
