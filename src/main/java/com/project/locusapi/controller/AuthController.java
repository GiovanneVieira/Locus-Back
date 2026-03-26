package com.project.locusapi.controller;

import com.project.locusapi.dto.auth.AuthRequestDTO;
import com.project.locusapi.dto.auth.AuthResponseDTO;
import com.project.locusapi.dto.user.UserRequestDTO;
import com.project.locusapi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerUser(@RequestBody @Valid UserRequestDTO userRequestDTO) {
        var response = this.authService.registerUser(userRequestDTO);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, response.cookie().toString()).body(response.responseDTO());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthRequestDTO userDto) {
        var response = authService.loginUser(userDto);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, response.cookie().toString()).body(response.responseDTO());
    }
}

