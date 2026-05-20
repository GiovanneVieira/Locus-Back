package com.project.locusapi.dto.forgotpassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ForgotPasswordDTO(String otpToken,
                                @Email(message = "enter a valid email") String email,
                                @Size(min = 8, message = "enter a valid password") String password) {
}
