package com.project.locusapi.dto.dashboard;

import com.project.locusapi.constant.dashboard.TaskColumnCode;

import java.util.List;
import java.util.UUID;

public record BoardColumnResponseDTO(
        UUID id,
        String title,
        TaskColumnCode code,
        Integer position,
        List<BoardTaskResponseDTO> tasks
) {
}