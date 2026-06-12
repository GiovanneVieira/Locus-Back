package com.project.locusapi.service.ai;

import com.project.locusapi.constant.AiProvider;
import com.project.locusapi.dto.destination.DestinationAIResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DestinationAIService {

    private final Map<AiProvider, AiProviderStrategy> strategyMap;

    public DestinationAIService(List<AiProviderStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(AiProviderStrategy::getProvider, Function.identity()));
    }

    public DestinationAIResponse recommendTouristPoints(String city) {
        String normalizedCity = normalizeCity(city);
        return getStrategy(AiProvider.GEMINI).recommendTouristPoints(normalizedCity);
    }

    private String normalizeCity(String city) {
        if (!StringUtils.hasText(city)) {
            throw new IllegalArgumentException("O parâmetro 'city' é obrigatório.");
        }
        return city.trim();
    }

    private AiProviderStrategy getStrategy(AiProvider provider) {
        AiProviderStrategy strategy = strategyMap.get(provider);
        if (strategy == null) {
            throw new IllegalArgumentException("Provedor de IA não implementado: " + provider);
        }
        return strategy;
    }
}
