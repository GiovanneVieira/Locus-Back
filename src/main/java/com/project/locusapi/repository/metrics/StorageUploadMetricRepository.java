package com.project.locusapi.repository.metrics;

import com.project.locusapi.model.metrics.StorageUploadMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface StorageUploadMetricRepository extends JpaRepository<StorageUploadMetric, UUID> {
    long countByOccurredAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.fileSize), 0) FROM StorageUploadMetric s WHERE s.occurredAt BETWEEN :start AND :end")
    long sumFileSizeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(AVG(s.fileSize), 0) FROM StorageUploadMetric s WHERE s.occurredAt BETWEEN :start AND :end")
    double averageFileSizeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
