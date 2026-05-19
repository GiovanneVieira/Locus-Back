package com.project.locusapi.dto.address.rentable;

import java.time.LocalDateTime;
import java.util.UUID;

public record RentableAddressImageResponseDTO(
        UUID id,
        String originalName,
        String s3Key,
        String contentType,
        Long fileSize,
        Boolean main,
        LocalDateTime createdAt
) {}
