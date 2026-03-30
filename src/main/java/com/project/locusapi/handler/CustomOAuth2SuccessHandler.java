package com.project.locusapi.handler;

import com.project.locusapi.service.AuthService;
import com.project.locusapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        var oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extraímos os dados que o Google nos deu
        assert oAuth2User != null;
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String pfpUrl = oAuth2User.getAttribute("picture");
        // Chamamos o serviço que agora sabe criar o usuário se ele não existir
        var authResult = authService.loginOAuth2User(email, name, pfpUrl);
        // Injetamos os Cookies no Header da resposta
        authResult.cookies().forEach(cookie ->
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
        );

        // Redireciona para o Dashboard no React
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/dashboard");
    }
}
