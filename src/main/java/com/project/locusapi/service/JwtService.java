package com.project.locusapi.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.project.locusapi.config.JwtPropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtPropertiesConfig jwtPropertiesConfig;

    @Value("${spring.dev.environment}")
    private String springEnv;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(jwtPropertiesConfig.getSecret());
    }

    public String generateAccessToken(UserDetails user) {
        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return JWT.create()
                .withIssuer(jwtPropertiesConfig.getIssuer())
                .withSubject(user.getUsername())
                .withClaim("roles", roles)
                .withClaim("tokenType", "access")// Access Token precisa de roles
                .withExpiresAt(Instant.now().plusSeconds(jwtPropertiesConfig.getJwtAccessExpiration()))
                .sign(getAlgorithm());
    }

    public String generateRefreshToken(UserDetails user) {
        return JWT.create()
                .withIssuer(jwtPropertiesConfig.getIssuer())
                .withSubject(user.getUsername())
                .withClaim("tokenType", "refresh")// Apenas quem é o dono
                .withExpiresAt(Instant.now().plusSeconds(jwtPropertiesConfig.getJwtRefreshExpiration()))
                .sign(getAlgorithm());
    }

    public String validateToken(String token) {
        return JWT.require(getAlgorithm())
                .withIssuer(jwtPropertiesConfig.getIssuer())
                .build()
                .verify(token)
                .getSubject();
    }

    public String getTokenType(String token) {
        return JWT.require(getAlgorithm())
                .withIssuer(jwtPropertiesConfig.getIssuer())
                .build()
                .verify(token)
                .getClaim("tokenType").asString();
    }

    public List<String> getRoles(String token) {
        return JWT.require(getAlgorithm())
                .withIssuer(jwtPropertiesConfig.getIssuer())
                .build()
                .verify(token)
                .getClaim("roles").asList(String.class);
    }

    private boolean isProd(){
        return "production".equalsIgnoreCase(springEnv);
    }

    public ResponseCookie getCleanCookie(String name) {

        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(isProd())
                .sameSite(isProd() ? "None" : "Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    private ResponseCookie generateCookie(String tokenName, String tokenValue, Integer maxAge) {

        return ResponseCookie
                .from(tokenName, tokenValue)
                .httpOnly(true)
                .secure(isProd())
                .maxAge(maxAge)
                .sameSite(isProd() ? "None" : "Lax")
                .path("/")
                .build();
    }

    public List<ResponseCookie> generateCookies(String accessToken, String refreshToken) {
        return List.of(generateCookie("accessToken", accessToken, jwtPropertiesConfig.getJwtAccessExpiration()),
                generateCookie("refreshToken", refreshToken, jwtPropertiesConfig.getJwtRefreshExpiration()));
    }
}
