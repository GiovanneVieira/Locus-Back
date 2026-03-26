package com.project.locusapi.controller;

import com.project.locusapi.dto.auth.AuthResponseDTO;
import com.project.locusapi.dto.auth.TokenRequestDTO;
import com.project.locusapi.service.AppUserDetailsService;
import com.project.locusapi.service.AuthService;
import com.project.locusapi.service.JwtService;
import com.project.locusapi.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/refresh")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService; // Use o service, não o Manager
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<AuthResponseDTO> refreshAccessToken(@RequestBody TokenRequestDTO tokenDTO) {
        var response = authService.refreshToken(tokenDTO.token());
        return ResponseEntity.ok(response);
    }
}
