package com.project.locusapi.controller;

import com.project.locusapi.dto.auth.AuthRequestDTO;
import com.project.locusapi.dto.auth.AuthResponseDTO;
import com.project.locusapi.dto.auth.AuthResultDTO;
import com.project.locusapi.dto.user.UserRequestDTO;
import com.project.locusapi.dto.user.UserResponseDTO;
import com.project.locusapi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody @Valid UserRequestDTO userRequestDTO) {
        var response = this.authService.registerUser(userRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthRequestDTO userDto) {
        // 1. O serviço devolve o AuthResultDTO completo (com os cookies e o DTO de resposta)
        AuthResultDTO result = authService.authenticateUser(userDto);

        // 2. Passamos a lista de cookies extraída do Record para o helper
        // 3. Definimos o corpo da resposta com o AuthResponseDTO contido no Record
        return prepareResponseWithCookies(result.cookies())
                .body(result.responseDTO());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Da mesma forma que no login, o refresh obtém o AuthResultDTO completo
        AuthResultDTO result = authService.refreshToken(refreshToken);

        return prepareResponseWithCookies(result.cookies())
                .body(result.responseDTO());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        // O logout não precisa do AuthResultDTO porque não há corpo de resposta com e-mail/token.
        // Ele precisa apenas da lista de cookies de limpeza.
        List<ResponseCookie> cleanCookies = authService.logoutUser(refreshToken);

        return prepareResponseWithCookies(cleanCookies)
                .body(Collections.singletonMap("message", "Logout realizado com sucesso"));
    }

    // HELPER ÚNICO: Centraliza a inserção de cookies no cabeçalho HTTP Set-Cookie
    private ResponseEntity.BodyBuilder prepareResponseWithCookies(List<ResponseCookie> cookies) {
        var responseBuilder = ResponseEntity.ok();

        cookies.forEach(cookie ->
                responseBuilder.header(HttpHeaders.SET_COOKIE, cookie.toString())
        );

        return responseBuilder;
    }
}