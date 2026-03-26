package com.project.locusapi.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.jwt.access.expiration}")
    private Integer jwtAccessExpiration;

    @Value("${api.jwt.refresh.expiration}")
    private Integer jwtRefreshExpiration;

    @Value("${api.jwt.issuer}")
    private String issuer;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }

    public String generateAccessToken(UserDetails user) {
        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getUsername())
                .withClaim("roles", roles)
                .withClaim("tokenType", "access")// Access Token precisa de roles
                .withExpiresAt(Instant.now().plusSeconds(jwtAccessExpiration))
                .sign(getAlgorithm());
    }

    public String generateRefreshToken(UserDetails user) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getUsername())
                .withClaim("tokenType", "refresh")// Apenas quem é o dono
                .withExpiresAt(Instant.now().plusSeconds(jwtRefreshExpiration))
                .sign(getAlgorithm());
    }

    public String validateToken(String token) {
        return JWT.require(getAlgorithm())
                .withIssuer(issuer)
                .build()
                .verify(token)
                .getSubject();
    }

    public String getTokenType(String token) {
        return JWT.require(getAlgorithm())
                .withIssuer(issuer)
                .build()
                .verify(token)
                .getClaim("tokenType").asString();
    }

    public List<String> getRoles(String token) {
        return JWT.require(getAlgorithm())
                .withIssuer(issuer)
                .build()
                .verify(token)
                .getClaim("roles").asList(String.class);
    }
}
