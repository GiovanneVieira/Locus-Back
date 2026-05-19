package com.project.locusapi.exception.business;

import java.util.UUID;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(UUID id) {
        super("Usuário com ID " + id + " não foi encontrado.");
    }

    public UserNotFoundException(String email) {
        super("Usuário com e-mail " + email + " não foi encontrado.");
    }

    public UserNotFoundException() {
        super("Usuário não encontrado.");
    }
}