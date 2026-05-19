package com.project.locusapi.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public record CEP(String value) {

    private static final String REGEX_CEP = "\\d{5}-\\d{3}";
    private static final String REGEX_CLEAN = "\\d{8}";

    public CEP {
        if (value == null) {
            throw new IllegalArgumentException("CEP não pode ser nulo.");
        }

        // Remove espaços e hífens para testar e padronizar
        String cleaned = value.replaceAll("\\D", "");

        if (!cleaned.matches(REGEX_CLEAN)) {
            throw new IllegalArgumentException("Formato de CEP inválido.");
        }

        // Armazena sempre formatado no padrão 12345-678
        value = cleaned.substring(0, 5) + "-" + cleaned.substring(5);
    }

    /**
     * Retorna apenas os números (útil para consultas ou integrações externas)
     */
    public String getOnlyDigits() {
        return value.replaceAll("\\D", "");
    }

    /**
     * Diz ao Jackson para serializar este objeto como uma String simples no JSON
     */
    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}