package com.project.locusapi.dto.rental;

import com.project.locusapi.constant.RentalStatus;
import jakarta.validation.constraints.NotNull;

public record RentalStatusUpdateDTO(
        @NotNull(message = "O novo status é obrigatório")
        RentalStatus status
) {}
