package com.project.locusapi.mapper.converter;

import com.project.locusapi.domain.CEP;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true) // Aplica automaticamente em qualquer entidade que use o tipo Cep
public class CepConverter implements AttributeConverter<CEP, String> {

    @Override
    public String convertToDatabaseColumn(CEP cep) {
        return cep != null ? cep.getOnlyDigits() : null; // Salva limpo no banco (ex: 18230000)
    }

    @Override
    public CEP convertToEntityAttribute(String dbData) {
        return dbData != null ? new CEP(dbData) : null; // Quando lê do banco, reconstrói o Record formatado
    }
}