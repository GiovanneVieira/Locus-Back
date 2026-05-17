package com.project.locusapi.dto.otp;

import jakarta.validation.constraints.Email;

public record OTPRequestDTO(
        @Email(message = "enter a valid email") String email, String username) {
}
