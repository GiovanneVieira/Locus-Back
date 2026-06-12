package com.project.locusapi.repository.metrics;

import com.project.locusapi.constant.RentalStatus;
import com.project.locusapi.model.metrics.RentalMetricEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface RentalMetricEventRepository extends JpaRepository<RentalMetricEvent, UUID> {
    long countByEventTypeAndOccurredAtBetween(String eventType, LocalDateTime start, LocalDateTime end);
    long countByEventTypeAndStatusAndOccurredAtBetween(String eventType, RentalStatus status, LocalDateTime start, LocalDateTime end);
}
