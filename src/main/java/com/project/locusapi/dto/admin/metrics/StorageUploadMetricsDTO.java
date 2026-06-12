package com.project.locusapi.dto.admin.metrics;

public record StorageUploadMetricsDTO(
        long uploadedImages,
        long totalBytes,
        double averageBytes
) {
}
