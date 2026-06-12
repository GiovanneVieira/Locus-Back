package com.project.locusapi.service.ai;

import com.project.locusapi.constant.AiProvider;
import com.project.locusapi.dto.destination.DestinationAIResponse;
import com.project.locusapi.exception.business.DestinationAIException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeminiAiProviderStrategy implements AiProviderStrategy {

    private final ChatClient customChatClient;
    private final DestinationAiPromptFactory promptFactory;
    private final DestinationAiResponseValidator responseValidator;

    @Override
    public AiProvider getProvider() {
        return AiProvider.GEMINI;
    }

    @Override
    public DestinationAIResponse recommendTouristPoints(String city) {
        try {
            DestinationAIResponse response = customChatClient.prompt()
                    .system(promptFactory.touristPointsSystemPrompt())
                    .user(promptFactory.touristPointsUserPrompt(city))
                    .call()
                    .entity(DestinationAIResponse.class);

            return responseValidator.validate(response);
        } catch (DestinationAIException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DestinationAIException("Não foi possível gerar recomendações turísticas no momento.");
        }
    }
}
