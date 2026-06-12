package com.project.locusapi.model.metrics;

import com.project.locusapi.constant.RentalStatus;
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
        name = "rental_metric_event_table",
        indexes = {
                @Index(name = "idx_rental_metric_event_status_time", columnList = "event_type, status, occurred_at"),
                @Index(name = "idx_rental_metric_rental", columnList = "rental_id")
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalMetricEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rental_id", nullable = false)
    private UUID rentalId;
    @Column(name = "renter_id")
    private UUID renterId;
    @Column(name = "rentable_address_id")
    private UUID rentableAddressId;
    @Column(name = "event_type", nullable = false)
    private String eventType;
    @Enumerated(EnumType.STRING)
    private RentalStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private RentalStatus previousStatus;
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
