package com.project.locusapi.dto.user;

import jakarta.validation.constraints.Email;

public record ActivateUserDTO (@Email(message = "enter a valid email") String email){
}
