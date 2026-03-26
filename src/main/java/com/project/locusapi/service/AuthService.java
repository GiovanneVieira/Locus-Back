package com.project.locusapi.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.project.locusapi.dto.auth.AuthRequestDTO;
import com.project.locusapi.dto.auth.AuthResponseDTO;
import com.project.locusapi.dto.auth.AuthResultDTO;
import com.project.locusapi.dto.user.UserRequestDTO;
import com.project.locusapi.mapper.UserMapper;
import com.project.locusapi.model.UserModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Use o Lombok para gerar o construtor com final
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final UserDetailsService userDetailsService;

    @Transactional // Importante para manter a sessão do banco aberta
    public AuthResultDTO registerUser(@Valid UserRequestDTO userDto) {
        // 1. Cria o usuário no banco (aqui o UserService faz o save)
        userService.createUser(userDto);

        // 2. Chama o método centralizado para autenticar e gerar resposta
        // O Manager vai buscar o usuário que acabamos de criar direto do banco!
        return authenticateAndGenerateResponse(userDto.email(), userDto.password());
    }

    public AuthResultDTO loginUser(@Valid AuthRequestDTO userDto) {
        return authenticateAndGenerateResponse(userDto.email(), userDto.password());
    }

    private AuthResultDTO authenticateAndGenerateResponse(String email, String password) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        var user = (UserModel) authentication.getPrincipal();

        // Gera os tokens
        assert user != null;
        String accessToken = jwtService.generateAccessToken(user);

        var savedRefreshToken = refreshTokenService.saveRefreshToken(jwtService.generateRefreshToken(user), user);

        // Sincroniza a lista bidirecional no modelo (Boa prática JPA)
        user.addRefreshToken(savedRefreshToken);

        return new AuthResultDTO(
                new AuthResponseDTO(user.getEmail(), accessToken, savedRefreshToken.getToken()),
                generateCookie(accessToken)
        );
    }

    @Transactional
    public AuthResponseDTO refreshToken(String oldToken) {
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

        // 3. Gera os novos
        var user = (UserModel) userDetailsService.loadUserByUsername(email);
        var newAccess = jwtService.generateAccessToken(user);
        var newRefreshStr = jwtService.generateRefreshToken(user);

        refreshTokenService.saveRefreshToken(newRefreshStr, user);

        return new AuthResponseDTO(user.getUsername(), newAccess, newRefreshStr);
    }

    private ResponseCookie generateCookie(String token) {
        return ResponseCookie
                .from("accessToken", token)
                .httpOnly(true)
                .secure(false) // Mude para true em produção
                .maxAge(3600 * 24 * 7)
                .sameSite("Lax")
                .path("/")
                .build();
    }
}