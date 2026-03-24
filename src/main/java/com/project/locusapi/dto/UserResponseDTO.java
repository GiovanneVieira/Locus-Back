package com.project.locusapi.dto;

import com.project.locusapi.constant.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(UUID id, String name, String email, Role role, LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
}
