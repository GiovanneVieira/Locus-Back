package com.project.locusapi.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequestDTO(

        @NotNull(message = "A nota é obrigatória")
        @Min(value = 1, message = "A nota mínima é 1 estrela")
        @Max(value = 5, message = "A nota máxima é 5 estrelas")
        Integer rating,

        @NotBlank(message = "O comentário é obrigatório")
        @Size(max = 600, message = "O comentário deve ter no máximo 600 caracteres")
        String comment
) {}
