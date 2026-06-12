package com.project.locusapi.exception.business;

import java.util.UUID;

public class DestinationNotFoundException extends ResourceNotFoundException {
    public DestinationNotFoundException(UUID id) {
        super("Destino não encontrado com o id: " + id);
    }

    public DestinationNotFoundException(String city) {
        super("Destino não encontrado para a cidade: " + city);
    }
}
