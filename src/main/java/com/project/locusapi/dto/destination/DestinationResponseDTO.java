package com.project.locusapi.dto.destination;

import java.util.List;
import java.util.UUID;

public record DestinationResponseDTO(
        UUID id,
        String country,
        String city,
        List<TouristPointResponseDTO> touristPoints
) {}
