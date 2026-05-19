package com.project.locusapi.exception.business;

import org.springframework.http.HttpStatus;

public class OtpExpiredException extends BusinessException {
    public OtpExpiredException() {
        super("O código de verificação expirou ou nunca foi solicitado.", HttpStatus.GONE);
    }
}