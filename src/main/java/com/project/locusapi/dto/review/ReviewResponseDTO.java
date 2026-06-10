package com.project.locusapi.dto.review;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponseDTO(
        UUID id,
        UUID addressId,
        UUID authorId,
        String authorName,
        String authorPfpUrl,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}
