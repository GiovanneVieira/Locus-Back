package com.project.locusapi.handler;

import com.project.locusapi.handler.OAuth2.OAuth2UserExtractor;
import com.project.locusapi.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final List<OAuth2UserExtractor> extractors;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        var oAuth2User = (OAuth2User) authentication.getPrincipal();
        var token = (OAuth2AuthenticationToken) authentication;
        String provider = token.getAuthorizedClientRegistrationId();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Encontra o extrator correto dinamicamente (OCP respeitado)
        String pfpUrl = extractors.stream()
                .filter(e -> e.supports(provider))
                .findFirst()
                .map(e -> e.extractProfilePicture(oAuth2User))
                .orElse(null);

        var authResult = authService.loginOAuth2User(email, name, pfpUrl, provider);

        // Injetamos os Cookies no Header da resposta
        authResult.cookies().forEach(cookie ->
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
        );

        // Redireciona para o Dashboard no React
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173");
    }
}