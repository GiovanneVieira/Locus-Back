package com.project.locusapi.model.metrics;

import com.project.locusapi.constant.metrics.CriticalFailureType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "critical_failure_metric_table",
        indexes = {
                @Index(name = "idx_failure_metric_type_time", columnList = "failure_type, occurred_at"),
                @Index(name = "idx_failure_metric_time", columnList = "occurred_at")
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriticalFailureMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(name = "failure_type", nullable = false)
    private CriticalFailureType failureType;
    @Column(columnDefinition = "TEXT")
    private String reason;
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
