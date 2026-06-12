package com.project.locusapi.dto.destination;

import java.util.List;

public record DestinationAIResponse(
        String destino,
        String pais,
        List<TouristPointDTO> pontosTuristicos
) {}
