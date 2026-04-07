package com.project.locusapi.dto.dashboard;

import java.util.List;
import java.util.UUID;

public record BoardResponseDTO(
        UUID id,
        String name,
        String description,
        List<BoardColumnResponseDTO> columns
) {
}