package com.project.locusapi.dto.dashboard;

import com.project.locusapi.constant.dashboard.TaskColumnCode;
import com.project.locusapi.constant.dashboard.TaskPriority;

import java.time.LocalDateTime;
import java.util.UUID;

public record BoardTaskResponseDTO(
        UUID id,
        String jiraCode,
        String title,
        String description,
        TaskPriority priority,
        Integer position,
        Integer storyPoints,
        String assignee,
        UUID columnId,
        TaskColumnCode columnCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}