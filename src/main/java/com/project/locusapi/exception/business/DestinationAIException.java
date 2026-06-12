package com.project.locusapi.exception.business;

import org.springframework.http.HttpStatus;

public class DestinationAIException extends BusinessException {
    public DestinationAIException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }
}
