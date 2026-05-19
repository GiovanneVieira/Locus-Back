package com.project.locusapi.exception.business;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException(String email) {
        super("O e-mail " + email + " já está cadastrado no sistema.", HttpStatus.CONFLICT);
    }
}