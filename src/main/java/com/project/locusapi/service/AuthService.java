package com.project.locusapi.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.project.locusapi.dto.auth.AuthRequestDTO;
import com.project.locusapi.dto.auth.AuthResponseDTO;
import com.project.locusapi.dto.auth.AuthResultDTO;
import com.project.locusapi.dto.user.UserRequestDTO;
import com.project.locusapi.dto.user.UserResponseDTO;
import com.project.locusapi.mapper.UserMapper;
import com.project.locusapi.model.UserModel;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;
    private final OTPService otpService;

    public UserResponseDTO registerUser(@Valid UserRequestDTO userDto) {
        return userService.createUser(userDto);
    }

    @Transactional
    public AuthResultDTO authenticateUser(@Valid AuthRequestDTO userDto) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.email(), userDto.password())
        );
        return generateAuthResult((UserModel) authentication.getPrincipal());
    }

    @Transactional
    public AuthResultDTO loginOAuth2User(String email, String name, String pfpUrl, String provider) {
        UserModel user = userService.getUserByEmail(email)
                .orElseGet(() -> userService.processOAuthUser(email, name, pfpUrl, provider));

        return generateAuthResult(user);
    }

    @Transactional
    public AuthResultDTO refreshToken(String oldToken) {
        // 1. Validações básicas via JwtService
        var email = jwtService.validateToken(oldToken);
        var type = jwtService.getTokenType(oldToken);

        if (email == null || !"refresh".equals(type)) {
            throw new JWTVerificationException("Invalid refresh token");
        }

        // 2. Valida no banco e deleta o antigo (Rotation)
        var storedToken = refreshTokenService.findByToken(oldToken);
        if (!storedToken.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Token ownership mismatch");
        }

        refreshTokenService.deleteByToken(oldToken);

        var user = (UserModel) userDetailsService.loadUserByUsername(email);

        var newAccessToken = jwtService.generateAccessToken(user);
        var newRefreshStr = jwtService.generateRefreshToken(user);

        refreshTokenService.saveRefreshToken(newRefreshStr, user);

        List<ResponseCookie> responseCookies = jwtService.generateCookies(newAccessToken, newRefreshStr);
        return new AuthResultDTO(new AuthResponseDTO(user.getUsername(), newAccessToken), responseCookies);
    }

    @Transactional
    public void logoutUser(String refreshToken, HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.deleteByToken(refreshToken);
        }

        ResponseCookie cleanAccess = jwtService.getCleanCookie("accessToken");
        ResponseCookie cleanRefresh = jwtService.getCleanCookie("refreshToken");

        response.addHeader(HttpHeaders.SET_COOKIE, cleanAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cleanRefresh.toString());
    }

    public AuthResultDTO generateAuthResult(UserModel user) {

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        var savedRefreshToken = refreshTokenService.saveRefreshToken(refreshToken, user);
        user.addRefreshToken(savedRefreshToken);

        List<ResponseCookie> responseCookies = jwtService.generateCookies(accessToken, refreshToken);

        return new AuthResultDTO(
                new AuthResponseDTO(user.getEmail(), accessToken),
                responseCookies);
    }

}
