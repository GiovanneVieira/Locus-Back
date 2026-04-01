package com.project.locusapi.dto.dashboard;

import com.project.locusapi.constant.dashboard.TaskPriority;

import java.util.UUID;

public record UpdateTaskRequestDTO(
        String jiraCode,
        String title,
        String description,
        TaskPriority priority,
        UUID columnId,
        Integer position,
        Integer storyPoints,
        String assignee
) {
}