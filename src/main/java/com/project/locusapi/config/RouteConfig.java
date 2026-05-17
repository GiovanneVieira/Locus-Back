package com.project.locusapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

@Configuration
public class RouteConfig {

    @Bean
    public List<String> publicRoutes() {
        return List.of(
                "/auth/register",
                "/auth/login",
                "/auth/refresh",
                "/oauth2/**",
                "/login/oauth2/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/user/activate",
                "/error",
                "/otp/**"
        );
    }

    @Bean
    public Map<HttpMethod, String[]> hostRoutes() {
        return Map.of(
                HttpMethod.POST, new String[]{"/address/rentable/**"},
                HttpMethod.PATCH, new String[]{"/address/rentable/**"},
                HttpMethod.DELETE, new String[]{"/address/rentable/**"}
        );
    }


}