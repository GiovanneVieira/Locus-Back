package com.project.locusapi.exception.business;

import java.util.UUID;

public class AddressNotFoundException extends ResourceNotFoundException {
    public AddressNotFoundException(UUID id, String tipo) {
        super(tipo + " com ID " + id + " não foi encontrado.");
    }

    public AddressNotFoundException(String message) {
        super(message);
    }
}