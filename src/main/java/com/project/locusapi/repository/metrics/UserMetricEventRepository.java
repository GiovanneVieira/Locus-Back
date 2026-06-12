package com.project.locusapi.repository.metrics;

import com.project.locusapi.constant.metrics.UserMetricEventType;
import com.project.locusapi.model.metrics.UserMetricEvent;
import com.project.locusapi.repository.metrics.projection.TimeBucketMetricProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserMetricEventRepository extends JpaRepository<UserMetricEvent, UUID> {

    long countByEventTypeAndOccurredAtGreaterThanEqual(UserMetricEventType eventType, LocalDateTime occurredAt);

    long countByEventTypeAndOccurredAtBetween(UserMetricEventType eventType, LocalDateTime start, LocalDateTime end);

    @Query(value = """
            SELECT CAST(date_trunc(:granularity, occurred_at) AS date) AS bucket, COUNT(*) AS total
            FROM user_metric_event_table
            WHERE event_type = :eventType
              AND occurred_at BETWEEN :start AND :end
            GROUP BY bucket
            ORDER BY bucket
            """, nativeQuery = true)
    List<TimeBucketMetricProjection> countByBucket(
            @Param("eventType") String eventType,
            @Param("granularity") String granularity,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
