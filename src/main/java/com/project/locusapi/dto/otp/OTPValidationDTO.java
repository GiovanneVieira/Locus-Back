package com.project.locusapi.dto.otp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record OTPValidationDTO(@Size(min = 6, message = "Enter a valid otp") String otpCode,
                               @Email(message = "enter a valid email") String email) {
}
