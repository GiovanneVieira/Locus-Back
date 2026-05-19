package com.project.locusapi.exception.business;

import org.springframework.http.HttpStatus;

public class PersonalAddressAlreadyExistsException extends BusinessException {
    public PersonalAddressAlreadyExistsException() {
        super("Este usuário já possui um endereço pessoal cadastrado.", HttpStatus.BAD_REQUEST);
    }
}