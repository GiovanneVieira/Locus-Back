package com.project.locusapi.repository.metrics;

import com.project.locusapi.model.metrics.LoginAccessMetric;
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
public interface LoginAccessMetricRepository extends JpaRepository<LoginAccessMetric, UUID> {

    long countBySuccessAndOccurredAtBetween(boolean success, LocalDateTime start, LocalDateTime end);

    Page<LoginAccessMetric> findByOccurredAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query(value = """
            SELECT device_type AS label, COUNT(*) AS total
            FROM login_access_metric_table
            WHERE success = true
              AND occurred_at BETWEEN :start AND :end
            GROUP BY device_type
            ORDER BY total DESC
            """, nativeQuery = true)
    List<MetricCountProjection> countSuccessfulLoginsByDevice(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT operating_system AS label, COUNT(*) AS total
            FROM login_access_metric_table
            WHERE success = true
              AND occurred_at BETWEEN :start AND :end
            GROUP BY operating_system
            ORDER BY total DESC
            """, nativeQuery = true)
    List<MetricCountProjection> countSuccessfulLoginsByOperatingSystem(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
