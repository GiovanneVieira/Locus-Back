package com.project.locusapi.service;

import com.project.locusapi.dto.AuthResponseDTO;
import com.project.locusapi.dto.AuthResultDTO;
import com.project.locusapi.dto.UserRequestDTO;
import com.project.locusapi.mapper.UserMapper;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final UserMapper userMapper;

    public AuthService(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager, AppUserDetailsService appUserDetailsService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.appUserDetailsService = appUserDetailsService;
        this.userMapper = new UserMapper();
    }

    public AuthResultDTO registerUser(UserRequestDTO userDto) {
        var savedUser = userService.createUser(userDto);
        var user = appUserDetailsService.loadUserByUsername(userDto.email());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDto.email(), userDto.password()));

        String token = jwtService.generateToken(user);

        return new AuthResultDTO(
                new AuthResponseDTO(user.getUsername(), token),
                generateCookie(token)
        );
    }

    public AuthResultDTO loginUser(UserRequestDTO userDto) {
        var authenticatedUser = appUserDetailsService.loadUserByUsername(userDto.email());
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.email(), userDto.password())
        );
        String token = jwtService.generateToken(authenticatedUser);
        return new AuthResultDTO(
                new AuthResponseDTO(token, authenticatedUser.getUsername()),
                generateCookie(token)
        );
    }

    private ResponseCookie generateCookie(String token) {
        return ResponseCookie
                .from("accessToken", token)
                .httpOnly(true)
                .maxAge(3600 * 24 * 7)
                .sameSite("Lax")
                .path("/")
                .build();
    }
}