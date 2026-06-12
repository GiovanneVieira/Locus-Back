package com.project.locusapi.dto.rental;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RentalRequestDTO(

        @NotNull(message = "A data de entrada é obrigatória")
        LocalDate checkIn,

        @NotNull(message = "A data de saída é obrigatória")
        LocalDate checkOut,

        @NotNull(message = "Informe o número de hóspedes")
        @Min(value = 1, message = "Deve haver pelo menos 1 hóspede")
        Integer guests,

        @Size(max = 300, message = "A mensagem deve ter no máximo 300 caracteres")
        String message
) {}
