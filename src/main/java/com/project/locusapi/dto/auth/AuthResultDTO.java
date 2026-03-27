package com.project.locusapi.dto.auth;

import org.springframework.http.ResponseCookie;

import java.util.List;

public record AuthResultDTO(AuthResponseDTO responseDTO, List<ResponseCookie> cookies) {
}
