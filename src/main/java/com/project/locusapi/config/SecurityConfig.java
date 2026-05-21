package com.project.locusapi.config;

import com.project.locusapi.filter.SecurityFilter;
import com.project.locusapi.handler.CustomOAuth2SuccessHandler;
import com.project.locusapi.service.AppUserDetailsService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;
    private final SecurityFilter securityFilter;
    private final List<String> publicRoutes;
    private final Map<HttpMethod, String[]> hostRoutes;

    @Value("${spring.url.front}")
    private String frontUrl;

    // Deixamos sem o 'final' para que o método @PostConstruct possa preenchê-la
    private List<String> authorizedOrigins;

    // O construtor volta a receber apenas os seus beans normais (sem Strings de properties)
    public SecurityConfig(AppUserDetailsService userDetailsService,
                          SecurityFilter securityFilter,
                          List<String> publicRoutes,
                          Map<HttpMethod, String[]> hostRoutes) {
        this.userDetailsService = userDetailsService;
        this.securityFilter = securityFilter;
        this.publicRoutes = publicRoutes;
        this.hostRoutes = hostRoutes;
    }

    @PostConstruct
    public void init() {
        log.info("Inicializando origens do CORS com a URL do Frontend: {}", frontUrl);
        this.authorizedOrigins = List.of("http://localhost:8080", frontUrl);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2SuccessHandler customOAuth2SuccessHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )
                .authorizeHttpRequests((authorize) -> {
                            authorize.requestMatchers(publicRoutes.toArray(String[]::new)).permitAll();
                            hostRoutes.forEach((method, patterns) ->
                                    authorize.requestMatchers(method, patterns).hasRole("HOST")
                            );
                            authorize.requestMatchers(HttpMethod.GET, "/s3/rentable-address/image/**").permitAll();
                            authorize.requestMatchers(HttpMethod.GET, "/address/rentable/**").permitAll();
                            authorize.requestMatchers(HttpMethod.PATCH, "/user/forgot-password").permitAll();
                            authorize.anyRequest().authenticated();
                        }
                ).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2.successHandler(customOAuth2SuccessHandler))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(authorizedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(authenticationProvider);
    }

}
