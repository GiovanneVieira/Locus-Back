package com.project.locusapi.dto.destination;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DestinationRequestDTO(
        @NotBlank(message = "O país é obrigatório")
        @Size(max = 120, message = "O país deve ter no máximo 120 caracteres")
        String country,

        @NotBlank(message = "A cidade é obrigatória")
        @Size(max = 120, message = "A cidade deve ter no máximo 120 caracteres")
        String city
) {}
