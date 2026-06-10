package com.project.locusapi.exception.business;

import java.util.UUID;

public class RentalNotFoundException extends ResourceNotFoundException {
    public RentalNotFoundException(UUID id) {
        super("Reserva com ID " + id + " não foi encontrada.");
    }
}
