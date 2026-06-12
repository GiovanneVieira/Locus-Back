package com.project.locusapi.exception.business;

import org.springframework.http.HttpStatus;

public class DestinationAlreadyExistsException extends BusinessException {
    public DestinationAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
