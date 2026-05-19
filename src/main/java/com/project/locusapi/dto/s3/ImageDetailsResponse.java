package com.project.locusapi.dto.s3;

import java.util.UUID;

public record ImageDetailsResponse(
        UUID id,
        String originalName,
        String s3Key
) {
}