package com.project.locusapi.model.metrics;

import com.project.locusapi.constant.metrics.LoginDeviceType;
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
        name = "login_access_metric_table",
        indexes = {
                @Index(name = "idx_login_metric_time", columnList = "occurred_at"),
                @Index(name = "idx_login_metric_device", columnList = "device_type"),
                @Index(name = "idx_login_metric_os", columnList = "operating_system")
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginAccessMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;
    private String email;
    @Column(nullable = false)
    private boolean success;
    private String reason;

    @Column(name = "raw_user_agent", columnDefinition = "TEXT")
    private String rawUserAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private LoginDeviceType deviceType;

    @Column(name = "operating_system", nullable = false)
    private String operatingSystem;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
