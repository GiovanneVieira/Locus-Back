package com.project.locusapi.exception.business;

import org.springframework.http.HttpStatus;

public abstract class ResourceNotFoundException extends BusinessException {
    protected ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}