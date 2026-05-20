package com.project.locusapi.exception.business;

import org.springframework.http.HttpStatus;

public class NewPasswordEqualsPreviousPassword extends BusinessException {
    public NewPasswordEqualsPreviousPassword() {
        super("Digite uma senha não utilizada", HttpStatus.BAD_REQUEST);
    }
}
