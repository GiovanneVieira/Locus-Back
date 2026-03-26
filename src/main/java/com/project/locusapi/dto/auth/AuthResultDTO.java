package com.project.locusapi.dto.auth;

import org.springframework.http.ResponseCookie;

public record AuthResultDTO(AuthResponseDTO responseDTO, ResponseCookie cookie) {
}
