package com.project.locusapi.service.ai;

import com.project.locusapi.dto.destination.DestinationAIResponse;
import com.project.locusapi.dto.destination.TouristPointDTO;
import com.project.locusapi.exception.business.DestinationAIException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class DestinationAiResponseValidator {

    private static final int REQUIRED_TOURIST_POINTS = 5;

    public DestinationAIResponse validate(DestinationAIResponse response) {
        if (response == null || !StringUtils.hasText(response.destino()) || !StringUtils.hasText(response.pais())) {
            throw new DestinationAIException("A IA retornou uma resposta de destino incompleta.");
        }

        List<TouristPointDTO> touristPoints = response.pontosTuristicos();
        if (touristPoints == null || touristPoints.size() != REQUIRED_TOURIST_POINTS) {
            throw new DestinationAIException("A IA não retornou exatamente 5 pontos turísticos.");
        }

        boolean hasInvalidPoint = touristPoints.stream().anyMatch(point -> point == null
                || !StringUtils.hasText(point.nome())
                || !StringUtils.hasText(point.descricao())
                || !StringUtils.hasText(point.categoria()));

        if (hasInvalidPoint) {
            throw new DestinationAIException("A IA retornou pontos turísticos com campos obrigatórios ausentes.");
        }

        return response;
    }
}
