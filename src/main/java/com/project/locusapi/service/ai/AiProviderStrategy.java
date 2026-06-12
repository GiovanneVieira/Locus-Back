package com.project.locusapi.service.ai;

import com.project.locusapi.constant.AiProvider;
import com.project.locusapi.dto.destination.DestinationAIResponse;

public interface AiProviderStrategy {
    AiProvider getProvider();

    DestinationAIResponse recommendTouristPoints(String city);
}
