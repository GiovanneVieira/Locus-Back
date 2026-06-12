package com.project.locusapi.model.metrics;

import com.project.locusapi.constant.AuthProvider;
import com.project.locusapi.constant.metrics.UserMetricEventType;
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
        name = "user_metric_event_table",
        indexes = {
                @Index(name = "idx_user_metric_event_type_time", columnList = "event_type, occurred_at"),
                @Index(name = "idx_user_metric_user", columnList = "user_id")
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMetricEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private UserMetricEventType eventType;

    @Column(nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private AuthProvider authProvider;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
