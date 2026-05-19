package com.project.locusapi.dto.s3;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record S3RequestDTO(
        @NotBlank(message = "O nome original do arquivo é obrigatório")
        String originalName,

        @NotBlank(message = "A chave (key) do S3 é obrigatória")
        String s3Key,

        @NotBlank(message = "O content type do arquivo é obrigatório")
        String contentType,

        @NotNull(message = "O tamanho do arquivo é obrigatório")
        @Positive(message = "O tamanho do arquivo deve ser maior que zero")
        Long fileSize
) {
}