package com.project.locusapi.filter;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.project.locusapi.service.AppUserDetailsService;
import com.project.locusapi.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final AppUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final HandlerExceptionResolver exceptionResolver;
    private final List<String> publicRoutes;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public SecurityFilter(JwtService jwtService, AppUserDetailsService userDetailsService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver, List<String> publicRoutes) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.exceptionResolver = exceptionResolver;
        this.publicRoutes = publicRoutes;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Se o caminho da requisição bater com qualquer um da lista pública, pula o filtro!
        return publicRoutes.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);
        try {

            if (token != null) {

                var tokenSubject = this.jwtService.validateToken(token);
                var tokenType = this.jwtService.getTokenType(token);
                if (tokenSubject != null && "access".equals(tokenType)) {
                    UserDetails user = this.userDetailsService.loadUserByUsername(tokenSubject);

                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } catch (TokenExpiredException ex) {
            jwtService.getCleanCookie("accessToken");
            exceptionResolver.resolveException(request, response, null, ex);
            return;
        } catch (Exception ex) {
            exceptionResolver.resolveException(request, response, null, ex);
            return;
        }
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}