package com.project.locusapi.constant;

import lombok.Getter;

@Getter
public enum AuthProvider {
    GOOGLE("google"),
    FACEBOOK("facebook"),
    DEFAULT("default");
    private final String value;

    AuthProvider(String value) {
        this.value = value;
    }
}
