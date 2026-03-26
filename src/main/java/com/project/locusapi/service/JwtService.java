package com.project.locusapi.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class JwtService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.jwt.access.expiration}")
    private Integer jwtAccessExpiration;

    @Value("${api.jwt.refresh.expiration}")
    private Integer jwtRefreshExpiration;

    public String generateToken(UserDetails user, long expirationTime) {
        try {
            List<String> roles = user.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            return JWT.create()
                    .withIssuer("locus-auth-api")
                    .withSubject(user.getUsername())
                    .withClaim("roles", roles)
                    .withExpiresAt(LocalDateTime.now().plusSeconds(expirationTime).toInstant(ZoneOffset.of("-03:00")))
                    .sign(Algorithm.HMAC256(secret));
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String generateAccessToken(UserDetails user) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return JWT.create()
                .withIssuer("locus-api")
                .withSubject(user.getUsername())
                .withClaim("roles", roles) // Access Token precisa de roles
                .withExpiresAt(Instant.now().plusSeconds(jwtAccessExpiration))
                .sign(algorithm);
    }

    public String generateRefreshToken(UserDetails user) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer("locus-api")
                .withSubject(user.getUsername()) // Apenas quem é o dono
                .withExpiresAt(Instant.now().plusSeconds(jwtRefreshExpiration))
                .sign(algorithm);
    }

    public String validateToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("locus-auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return "";
        }
    }
}
