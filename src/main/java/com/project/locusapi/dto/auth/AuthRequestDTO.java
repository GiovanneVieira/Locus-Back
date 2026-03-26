package com.project.locusapi.dto.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequestDTO(@Email(message = "enter a valid email")
                             String email,
                             @NotBlank(message = "password is required")
                             @Size(min = 8)
                             String password) {
}
