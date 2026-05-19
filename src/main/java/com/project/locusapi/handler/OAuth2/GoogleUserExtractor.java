package com.project.locusapi.handler.OAuth2;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleUserExtractor implements OAuth2UserExtractor {
    @Override
    public boolean supports(String provider) {
        return "google".equalsIgnoreCase(provider);
    }
    @Override
    public String extractProfilePicture(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("picture");
    }
}
