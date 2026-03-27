package com.project.locusapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RouteConfig {

    @Bean
    public List<String> publicRoutes() {
        return List.of(
                "/user/**",
                "/auth/register",
                "/auth/login",
                "/refresh",
                "/error"
        );
    }
}