package com.project.locusapi.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.regex.Pattern;

public record CPF(String cpf) {

    private static final Pattern CLEAN_PATTERN = Pattern.compile("[.\\-]");

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public CPF {
        if (cpf == null) {
            throw new IllegalArgumentException("CPF should not be null");
        }

        String cleanedCpf = cpf.replaceAll("[.\\-]", "");

        // Um CPF limpo precisa ter exatamente 11 dígitos numéricos
        if (cleanedCpf.length() != 11 || !cleanedCpf.matches("\\d{11}")) {
            throw new IllegalArgumentException("Invalid CPF format");
        }

        if (!isCpfValid(cleanedCpf)) {
            throw new IllegalArgumentException("Invalid CPF digits");
        }

        // Formatação clássica utilizando String.format
        cpf = String.format("%s.%s.%s-%s",
                cleanedCpf.substring(0, 3),
                cleanedCpf.substring(3, 6),
                cleanedCpf.substring(6, 9),
                cleanedCpf.substring(9, 11)
        );
    }

    private boolean isCpfValid(String cleanCpf) {
        // CPFs com todos os dígitos iguais são matematicamente válidos pela fórmula, mas são rejeitados pela Receita
        if (cleanCpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int[] digits = new int[11];
        for (int i = 0; i < 11; i++) {
            digits[i] = Character.getNumericValue(cleanCpf.charAt(i));
        }

        int jValue = calculateDigit(digits, 9, 10);
        if (jValue != digits[9]) {
            return false;
        }

        int kValue = calculateDigit(digits, 10, 11);
        return kValue == digits[10];
    }

    // Unificamos a lógica do J e K em um único método reaproveitável
    private int calculateDigit(int[] digits, int length, int weightStart) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += digits[i] * (weightStart - i);
        }
        int remainder = sum % 11;
        return (remainder < 2) ? 0 : (11 - remainder);
    }
}