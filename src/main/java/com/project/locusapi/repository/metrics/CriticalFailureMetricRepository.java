package com.project.locusapi.repository.metrics;

import com.project.locusapi.model.metrics.CriticalFailureMetric;
import com.project.locusapi.repository.metrics.projection.MetricCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CriticalFailureMetricRepository extends JpaRepository<CriticalFailureMetric, UUID> {
    long countByOccurredAtBetween(LocalDateTime start, LocalDateTime end);
    Page<CriticalFailureMetric> findByOccurredAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query(value = """
            SELECT failure_type AS label, COUNT(*) AS total
            FROM critical_failure_metric_table
            WHERE occurred_at BETWEEN :start AND :end
            GROUP BY failure_type
            ORDER BY total DESC
            """, nativeQuery = true)
    List<MetricCountProjection> countByFailureType(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
