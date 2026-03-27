package com.project.locusapi.controller;

import com.project.locusapi.dto.auth.AuthRequestDTO;
import com.project.locusapi.dto.auth.AuthResponseDTO;
import com.project.locusapi.dto.auth.AuthResultDTO;
import com.project.locusapi.dto.user.UserRequestDTO;
import com.project.locusapi.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerUser(@RequestBody @Valid UserRequestDTO userRequestDTO) {
        var result = this.authService.registerUser(userRequestDTO);
        return assembleResponse(result);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthRequestDTO userDto) {
        var result = authService.loginUser(userDto);
        return assembleResponse(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var result = authService.refreshToken(refreshToken);
        return assembleResponse(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        authService.logoutUser(refreshToken, response);
        return ResponseEntity.ok(Collections.singletonMap("message", "Logout realizado com sucesso"));
    }

    // Helper privado para não repetir a lógica de adicionar cookies no Header
    private ResponseEntity<AuthResponseDTO> assembleResponse(AuthResultDTO result) {
        var responseBuilder = ResponseEntity.ok();

        // Adiciona cada cookie da lista (Access e Refresh) no cabeçalho Set-Cookie
        result.cookies().forEach(cookie ->
                responseBuilder.header(HttpHeaders.SET_COOKIE, cookie.toString())
        );

        return responseBuilder.body(result.responseDTO());
    }
}

