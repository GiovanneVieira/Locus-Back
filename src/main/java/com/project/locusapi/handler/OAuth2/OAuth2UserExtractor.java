package com.project.locusapi.handler.OAuth2;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserExtractor {
    boolean supports(String provider);
    String extractProfilePicture(OAuth2User oAuth2User);
}
