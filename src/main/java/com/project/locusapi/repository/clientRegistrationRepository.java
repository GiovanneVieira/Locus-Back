package com.project.locusapi.repository;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

public interface clientRegistrationRepository {
    ClientRegistration getClientRegistration(String clientRegistrationId);
}
