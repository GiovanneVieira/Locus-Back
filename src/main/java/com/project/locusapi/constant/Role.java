package com.project.locusapi.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Role {
    ADMIN("ROLE_ADMIN"),
    HOST("ROLE_HOST"),
    USER("ROLE_USER");

    @JsonValue
    private final String roleString;

    Role(String roleString) {
        this.roleString = roleString;
    }

    public static Role fromString(String roleString) {
        for (Role role : Role.values()) {
            if (role.getRoleString().equalsIgnoreCase(roleString)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid Role");
    }
}
