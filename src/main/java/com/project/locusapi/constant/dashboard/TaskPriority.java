package com.project.locusapi.constant.dashboard;

import lombok.Getter;

@Getter
public enum TaskPriority {

    HIGH("high"),
    MEDIUM("medium"),
    LOW("low"),
    LOWEST("lowest");

    private final String value;

    TaskPriority(final String value) {
        this.value = value;
    }
}
