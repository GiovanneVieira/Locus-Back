package com.project.locusapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(String name, @Email(message = "enter a valid email") String email,  @NotBlank(message = "password is required") @Size(min = 8, message = "password requires at least 8 characters") String password) {
}
