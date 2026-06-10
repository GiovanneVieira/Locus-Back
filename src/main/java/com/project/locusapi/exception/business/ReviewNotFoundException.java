package com.project.locusapi.exception.business;

import java.util.UUID;

public class ReviewNotFoundException extends ResourceNotFoundException {
    public ReviewNotFoundException(UUID id) {
        super("Avaliação com ID " + id + " não foi encontrada.");
    }
}
