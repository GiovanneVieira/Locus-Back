package com.project.locusapi.dto;

import org.springframework.http.ResponseCookie;

public record AuthResultDTO(AuthResponseDTO responseDTO, ResponseCookie cookie) {
}
