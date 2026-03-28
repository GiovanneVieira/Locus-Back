package com.project.locusapi.service;

import com.project.locusapi.config.JwtPropertiesConfig;
import com.project.locusapi.model.RefreshToken;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.repository.RefreshTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtPropertiesConfig jwtPropertiesConfig;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtPropertiesConfig jwtPropertiesConfig) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtPropertiesConfig = jwtPropertiesConfig;
    }

    public RefreshToken saveRefreshToken(String token, UserModel user) {

        var refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(jwtPropertiesConfig.getJwtRefreshExpiration()))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElseThrow(() -> new EntityNotFoundException("Token " + token + " not found"));
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

}
