package com.project.locusapi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("properties")
@Configuration
@Getter
public class JwtPropertiesConfig {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.jwt.access.expiration}")
    private Integer jwtAccessExpiration;

    @Value("${api.jwt.refresh.expiration}")
    private Integer jwtRefreshExpiration;

    @Value("${api.jwt.issuer}")
    private String issuer;
    
}
