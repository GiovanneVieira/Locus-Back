package com.project.locusapi.event.metrics;

import com.project.locusapi.constant.RentalStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RentalCreatedEvent(
        UUID rentalId,
        UUID renterId,
        UUID rentableAddressId,
        RentalStatus status,
        LocalDateTime occurredAt
) {
}
