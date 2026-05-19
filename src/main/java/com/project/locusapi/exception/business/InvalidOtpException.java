package com.project.locusapi.exception.business;

import org.springframework.http.HttpStatus;

public class InvalidOtpException extends BusinessException {
    public InvalidOtpException() {
        super("Código de verificação inválido.", HttpStatus.BAD_REQUEST);
    }
}