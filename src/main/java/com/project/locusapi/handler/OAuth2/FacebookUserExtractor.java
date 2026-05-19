package com.project.locusapi.handler.OAuth2;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class FacebookUserExtractor implements OAuth2UserExtractor {
    @Override
    public boolean supports(String provider) {
        return "facebook".equalsIgnoreCase(provider);
    }

    @Override
    public String extractProfilePicture(OAuth2User oAuth2User) {
        Object pictureObj = oAuth2User.getAttribute("picture");
        if (pictureObj instanceof java.util.Map<?, ?> pictureMap) {
            var dataMap = (java.util.Map<?, ?>) pictureMap.get("data");
            if (dataMap != null) {
                return (String) dataMap.get("url");
            }
        }
        return null;
    }
}
