package com.project.locusapi.config;

import com.google.genai.Client;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class GoogleGenAiConfig {

    @Bean
    @Primary
    public GoogleGenAiChatModel customGoogleGenAiChatModel(
            @Value("${spring.ai.google.genai.api-key}") String apiKey) {

        Client genAiClient = Client.builder().apiKey(apiKey).build();

        return GoogleGenAiChatModel.builder()
                .genAiClient(genAiClient)
                .defaultOptions(GoogleGenAiChatOptions.builder().model("gemini-3.5-flash").build())
                .build();
    }

    @Bean
    public ChatClient customChatClient(GoogleGenAiChatModel customGoogleGenAiChatModel) {
        return ChatClient.create(customGoogleGenAiChatModel);
    }
}
