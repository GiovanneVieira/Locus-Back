package com.project.locusapi.controller;

import com.project.locusapi.dto.AuthResponseDTO;
import com.project.locusapi.dto.UserRequestDTO;
import com.project.locusapi.service.AuthService;
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
    public ResponseEntity<?> registerUser(@RequestBody UserRequestDTO userRequestDTO) {
        var response = this.authService.registerUser(userRequestDTO);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, response.cookie().toString()).body(response.responseDTO());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody UserRequestDTO userDto) {
        var response = authService.loginUser(userDto);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, response.cookie().toString()).body(response.responseDTO());
    }
}

