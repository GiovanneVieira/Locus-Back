package com.project.locusapi.dto.dashboard;

import com.project.locusapi.constant.dashboard.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTaskRequestDTO(
        @NotBlank String jiraCode,
        @NotBlank String title,
        String description,
        @NotNull TaskPriority priority,
        @NotNull UUID columnId,
        Integer position,
        Integer storyPoints,
        String assignee
) {
}