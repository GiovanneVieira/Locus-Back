package com.project.locusapi.dto.destination;

import java.util.UUID;

public record TouristPointResponseDTO(
        UUID id,
        String name,
        String description,
        String category
) {}
