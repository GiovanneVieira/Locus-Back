package com.project.locusapi.model.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "storage_upload_metric_table",
        indexes = {
                @Index(name = "idx_storage_metric_time", columnList = "occurred_at"),
                @Index(name = "idx_storage_metric_host", columnList = "host_id")
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageUploadMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "image_id", nullable = false)
    private UUID imageId;
    @Column(name = "host_id", nullable = false)
    private UUID hostId;
    @Column(name = "file_size", nullable = false)
    private long fileSize;
    @Column(name = "content_type")
    private String contentType;
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
